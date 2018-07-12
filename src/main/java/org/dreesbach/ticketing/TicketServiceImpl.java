package org.dreesbach.ticketing;

import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of TicketService interface.
 */
final class TicketServiceImpl implements TicketService {
    /**
     * How often (in seconds) to check {@link SeatHold} expiration. This can be tuned a bit if there are lots of seat holds and
     * we don't care if the holds aren't removed immediately, but want to prioritize throughput of new holds.
     */
    private static final long CHECK_SEAT_HOLD_EXPIRATION_MILLISECONDS = 1_000L;
    /**
     * Default seat hold expiration time.
     */
    private static final Duration DEFAULT_SEAT_HOLD_EXPIRATION_TIME = Duration.ofMinutes(5);
    /**
     * How long it takes for seat holds to expire.
     */
    private final Duration seatHoldExpirationTime;
    /**
     * A seating arrangement passed into this class.
     * <p>
     * Can be a simple rectangular arrangement, or more complex.
     */
    private SeatingArrangement seatingArrangement;
    /**
     * List of all the seat holds.
     */
    private Queue<SeatHold> seatHolds;
    /**
     * An executor service to periodically go through existing {@link SeatHold}s and expire them if they have exceeded their
     * maximum lifetime.
     */
    private ScheduledExecutorService seatHoldExpiration = new ScheduledThreadPoolExecutor(1);

    /**
     * Default constructor.
     *
     * @param seatingArrangement an implementation of {@link SeatingArrangement}
     */
    TicketServiceImpl(final SeatingArrangement seatingArrangement) {
        this(seatingArrangement, CHECK_SEAT_HOLD_EXPIRATION_MILLISECONDS, DEFAULT_SEAT_HOLD_EXPIRATION_TIME);
    }

    /**
     * Constructor allowing specification of seat hold expiration time.
     *
     * @param seatingArrangement an implementation of {@link SeatingArrangement}
     * @param seatHoldCheckExpirationMilliseconds how often we should check for seat hold expiration
     * @param seatHoldExpirationTime how long until a {@link SeatHold} expires
     */
    TicketServiceImpl(
            final SeatingArrangement seatingArrangement,
            final long seatHoldCheckExpirationMilliseconds,
            final Duration seatHoldExpirationTime
    ) {
        this.seatingArrangement = seatingArrangement;
        this.seatHoldExpirationTime = seatHoldExpirationTime;
        seatHolds = new ArrayBlockingQueue<>(seatingArrangement.getTotalNumSeats());
        // We don't want executions to pile up, so we use scheduleWithFixedDelay rather than scheduleAtFixedRate
        seatHoldExpiration.scheduleWithFixedDelay(this::expireSeatHolds,
                0L,
                seatHoldCheckExpirationMilliseconds,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public int numSeatsAvailable() {
        return seatingArrangement.getAvailableNumSeats();
    }

    /**
     * This implementation may return a {@code null} when no seats are available. The client code is expected to check and
     * handle this.
     * <p>
     * The returned SeatHold may have fewer seats than were requested if that many were not available.
     *
     * @param numSeats the number of seats to find and hold
     * @param customerEmail unique identifier for the customer
     * @return either a {@link SeatHold} or {@code null} when no seats are available
     */
    @Override
    public synchronized SeatHold findAndHoldSeats(final int numSeats, final String customerEmail) {
        // TODO: delegate this to SeatingArrangement?
        SeatHold seatHold = new SeatHold(numSeats, seatingArrangement, seatHoldExpirationTime);
        int seatsAvailableToHold = seatHold.getNumSeatsHeld();
        if (seatsAvailableToHold == 0) {
            /*
                I am choosing to return null here, which of course places a burden of null-checking on the calling code.
                However this means that there is no further memory/processing load on this code, so that is an optimization
                I'm making here.
                Another option would be to throw an exception, but since it is a regular occurrence that there will be no
                seats available, throwing and processing that exception will be costly in a high-demand environment.
                Lastly, I could return a SeatHold with 0 seats, however in that case it's another object to allocate and
                track, so I'm opting for the client code having to null-check instead.

                TODO: Change this to return Optional<SeatHold> instead
             */
            return null;
        }
        if (!seatHolds.offer(seatHold)) {
            throw new IllegalStateException("Somehow more seats were allocated than the venue can hold - this should never "
                    + "happen and must be investigated");
        }
        return seatHold;
    }

    @Override
    public String reserveSeats(final int seatHoldId, final String customerEmail) {
        return "Reservation code";
    }

    /**
     * How many seat holds are currently in effect.
     *
     * @return number of seats held
     */
    public int numSeatsHeld() {
        // Traversing the queue every time may get slow, however other than in tests this method isn't being used yet, so
        // rather than prematurely optimizing this and possibly having complications from tracking a separate "numSeatsHeld"
        // count that could easily get out of sync, we'll leave this as-is for now and come back to optimizing it later if it
        // is determined to be an issue.
        int seatsHeld = 0;
        for (SeatHold seatHold : seatHolds) {
            seatsHeld += seatHold.getNumSeatsHeld();
        }
        return seatsHeld;
    }

    /**
     * Removes expired {@link SeatHold}s.
     * <p>
     * TODO: figure out if this needs to be synchronized.
     */
    private void expireSeatHolds() {
        while (seatHolds.peek() != null) {
            if (seatHolds.peek().expired()) {
                SeatHold expiredSeatHold = seatHolds.remove();
                expiredSeatHold.remove();
            }
        }
    }
}
