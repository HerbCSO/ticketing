package org.dreesbach.ticketing;

import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of TicketService interface.
 */
public final class TicketServiceImpl implements TicketService {
    /**
     * How often (in seconds) to check {@link SeatHold} expiration. This can be tuned a bit if there are lots of seat holds and
     * we don't care if the holds aren't removed immediately, but want to prioritize throughput of new holds.
     */
    private static final Duration CHECK_SEAT_HOLD_EXPIRATION_DURATION = Duration.ofSeconds(1);
    /**
     * Default seat hold expiration time.
     */
    private static final Duration DEFAULT_SEAT_HOLD_EXPIRATION_TIME = Duration.ofMinutes(5);
    /**
     * Minimum acceptable length for an email string.
     */
    private static final int MIN_EMAIL_STRING_LENGTH = 3;
    /**
     * How long it takes for seat holds to expire.
     */
    private final Duration seatHoldExpirationTime;
    /**
     * A venue passed into this class.
     * <p>
     * Can be a simple rectangular arrangement, or more complex.
     */
    private Venue venue;
    /**
     * List of all the seat holds.
     */
    private Map<Integer, SeatHold> seatHolds;
    /**
     * An executor service to periodically go through existing {@link SeatHold}s and expire them if they have exceeded their
     * maximum lifetime.
     */
    private ScheduledExecutorService seatHoldExpiration = new ScheduledThreadPoolExecutor(1);

    /**
     * Default constructor.
     *
     * @param venue an implementation of {@link Venue}
     */
    TicketServiceImpl(final Venue venue) {
        this(venue, CHECK_SEAT_HOLD_EXPIRATION_DURATION, DEFAULT_SEAT_HOLD_EXPIRATION_TIME);
    }

    /**
     * Constructor allowing specification of seat hold expiration time.
     *
     * @param venue an implementation of {@link Venue}
     * @param seatHoldCheckExpiration how often we should check for seat hold expiration
     * @param seatHoldExpirationTime how long until a {@link SeatHold} expires
     */
    TicketServiceImpl(
            final Venue venue, final Duration seatHoldCheckExpiration, final Duration seatHoldExpirationTime
    ) {
        checkNotNull(seatHoldCheckExpiration, "seatHoldCheckExpiration must not be null");
        checkArgument(
                !(seatHoldCheckExpiration.isNegative() || seatHoldCheckExpiration.isZero()),
                "seatHoldCheckExpiration must be > 0"
        );
        this.venue = checkNotNull(venue, "venue cannot be null");
        this.seatHoldExpirationTime = checkNotNull(seatHoldExpirationTime, "seatHoldExpirationTime cannot be null");
        // We assign a map sized as per the total capacity of the venue, meaning we can support SeatHolds of size 1 for
        // every seat there. This is probably overkill, however it should ensure that the map never needs to grow, keeping
        // throughput constant.
        seatHolds = Collections.synchronizedMap(new LinkedHashMap<>(venue.getTotalNumSeats()));
        // We don't want executions to pile up, so we use scheduleWithFixedDelay rather than scheduleAtFixedRate
        var unused = seatHoldExpiration.scheduleWithFixedDelay(this::expireSeatHolds,
                0L,
                seatHoldCheckExpiration.toMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public int numSeatsAvailable() {
        return venue.getAvailableNumSeats();
    }

    /**
     * This implementation may return a {@code null} when no seats are available. The client code is expected to check and
     * handle this.
     * <p>
     * The returned SeatHold may have fewer seats than were requested if that many were not available.
     *
     * @param numSeatsToHold the number of seats to find and hold
     * @param customerEmail unique identifier for the customer
     * @return either a {@link SeatHold} or {@code null} when no seats are available
     */
    @Override
    public synchronized SeatHold findAndHoldSeats(final int numSeatsToHold, final String customerEmail) {
        checkArgument(numSeatsToHold > 0, "numSeatsToHold must be > 0");
        checkNotNull(customerEmail, "customerEmail cannot be null");
        checkEmailParam(customerEmail);
        SeatHold seatHold = venue.holdSeats(numSeatsToHold, seatHoldExpirationTime);
        if (seatHolds.containsKey(seatHold.getId())) {
            throw new IllegalStateException("Tried to allocate the same SeatHold ID [" + seatHold.getId() + "] more than once");
        }
        else {
            seatHolds.put(seatHold.getId(), seatHold);
        }
        return seatHold;
    }

    /**
     * @throws IllegalStateException when a SeatHold is not found
     */
    @Override
    public String reserveSeats(final int seatHoldId, final String customerEmail) {
        checkArgument(seatHoldId > 0, "seatHoldId must be > 0");
        checkEmailParam(customerEmail);
        SeatHold seatHold;
        if (!seatHolds.containsKey(seatHoldId)) {
            throw new IllegalStateException("SeatHold ID [" + seatHoldId + "] not found");
        }
        else {
            seatHold = seatHolds.get(seatHoldId);
            if (seatHold.expired()) {
                throw new IllegalStateException("SeatHold ID [" + seatHoldId + "] is expired");
            }
        }
        return venue.reserve(seatHold);
    }

    /**
     * Convenience method to check emails are "valid". Only checks minimal length requirement right now, but could be expanded
     * to do more.
     *
     * @param customerEmail email address to check
     */
    private void checkEmailParam(final String customerEmail) {
        checkArgument(customerEmail.length() > MIN_EMAIL_STRING_LENGTH,
                "C'mon, you think [%s] is an email address!? ;]",
                customerEmail
        );
    }

    /**
     * How many seat holds are currently in effect.
     *
     * @return number of seats held
     */
    public int numSeatsHeld() {
        // Traversing the map every time may get slow, however other than in tests this method isn't being used yet, so
        // rather than prematurely optimizing this and possibly having complications from tracking a separate "numSeatsHeld"
        // count that could easily get out of sync, we'll leave this as-is for now and come back to optimizing it later if it
        // is determined to be an issue.
        int seatsHeld = 0;
        for (SeatHold seatHold : seatHolds.values()) {
            seatsHeld += seatHold.getNumSeatsHeld();
        }
        return seatsHeld;
    }

    /**
     * Removes expired {@link SeatHold}s.
     */
    private synchronized void expireSeatHolds() {
        // This is easy, but will iterate over the entire map every time:
        //   seatHolds.entrySet().removeIf(e -> e.getValue().expired());
        // so instead we're doing this the old-fashioned way so that we can stop once we hit the first non-expired entry
        for (Iterator<Map.Entry<Integer, SeatHold>> it = seatHolds.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, SeatHold> entry = it.next();
            if (entry.getValue().expired()) {
                venue.removeHold(entry.getValue());
                it.remove();
            }
            else {
                // we've gotten to the end of the expired entries, since they're sorted by insertion order, so quit here
                return;
            }
        }
    }
}
