package org.dreesbach.ticketing;

import com.google.common.collect.ImmutableList;
import org.dreesbach.ticketing.id.IdGenerator;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class to coordinate holding of seats prior to actually reserving.
 */
class SeatHold {
    /** How long the seat hold is kept before expiring. */
    private static final Duration SEAT_HOLD_EXPIRATION_TIME = Duration.ofMinutes(5);
    /** The expiration time of this {@code SeatHold}. */
    private Instant expirationTime;
    /** Number of seats to hold for reservation. */
    private int numSeatsRequested;
    /** Unique ID of the seat hold. */
    private int id;
    /** List of seats held. */
    private List<Seat> seatsHeld = new ArrayList<>();

    /**
     * Create a new SeatHold.
     *
     * Will auto-generate an ID.
     *
     * @param seatsToHold list of seats to hold
     */
    SeatHold(final List<Seat> seatsToHold) {
        this(seatsToHold, SEAT_HOLD_EXPIRATION_TIME);
    }

    /**
     * Create a new SeatHold with the specified expiration time.
     *
     * Note that we support SeatHolds with 0 seats held to enable easier handling of cases where the venue is already at full
     * capacity, i.e. no seats are left. In that case we want to return an essentially empty SeatHold to avoid having to
     * throw an exception or returning a null. It effectively becomes a null object in this scenario.
     *
     * @param seatsToHold list of seats to hold
     * @param seatHoldExpirationTime duration until the {@code SeatHold} expires
     */
    SeatHold(final List<Seat> seatsToHold, final Duration seatHoldExpirationTime) {
        checkNotNull(seatsToHold, "seatsToHold cannot be null");
        checkNotNull(seatHoldExpirationTime, "seatHoldExpirationTime cannot be null");
        numSeatsRequested = seatsToHold.size();
        for (Seat seat : seatsToHold) {
            seat.hold();
            seatsHeld.add(seat);
        }
        id = IdGenerator.generateUniqueIntId();
        expirationTime = Instant.now().plus(seatHoldExpirationTime);
    }

    /**
     * Number of seats held for this instance.
     *
     * @return number of seats held
     */
    public int getNumSeatsHeld() {
        return seatsHeld.size();
    }

    /**
     * Get the actual seats held by this.
     *
     * @return a list of {@link Seat}s
     */
    public List<Seat> getSeatsHeld() {
        return ImmutableList.copyOf(seatsHeld);
    }

    /**
     * Number of seats requested for this instance.
     *
     * @return number of seats requested
     */
    public int getNumSeatsRequested() {
        return numSeatsRequested;
    }

    /**
     * Unique integer ID for the seat hold.
     *
     * Note: I would prefer to do this as a UUID in order to prevent potentially leaking this information out and making it
     * guessable, thus potentially enabling someone to get somebody else's seat hold (although they'd need to know their
     * email as well, making it a little less likely to happen). However the interface has been specified with this as an
     * {@code int}, so I'm stuck with this implementation.
     *
     * @return unique ID of the seat hold
     */
    public int getId() {
        return id;
    }

    /**
     * Whether this {@code SeatHold} has expired.
     *
     * @return true if it is after the expiration time
     */
    public boolean expired() {
        return Instant.now().isAfter(expirationTime) || Instant.now().equals(expirationTime);
    }

    /**
     * Remove this {@code SeatHold}.
     */
    public void remove() {
        IdGenerator.retireId(getId());
        if (expirationTime.isAfter(Instant.now())) {
            expirationTime = Instant.now();
        }
        seatsHeld.stream().forEach(Seat::cancelHold);
        seatsHeld = Collections.emptyList();
    }
}
