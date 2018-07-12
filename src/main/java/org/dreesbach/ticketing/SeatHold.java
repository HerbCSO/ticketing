package org.dreesbach.ticketing;

import org.dreesbach.ticketing.id.IdGenerator;

import java.time.Duration;
import java.time.Instant;

/**
 * Class to coordinate holding of seats prior to actually reserving.
 */
class SeatHold {
    /**
     * How long the seat hold is kept before expiring.
     */
    private static final Duration SEAT_HOLD_EXPIRATION_TIME = Duration.ofMinutes(5);
    /**
     * The expiration time of this {@code SeatHold}.
     */
    private Instant expirationTime;
    /**
     * Number of seats to hold for reservation.
     */
    private int numSeats;
    /**
     * Unique ID of the seat hold.
     */
    private int id;

    /**
     * Create a new SeatHold.
     *
     * Will auto-generate an ID.
     *
     * @param numSeats number of seats to hold
     */
    SeatHold(final int numSeats) {
        this(numSeats, SEAT_HOLD_EXPIRATION_TIME);
    }

    /**
     * Create a new SeatHold with the specified expiration time.
     *
     * @param numSeats number of seats to hold
     * @param seatHoldExpirationTime duration until the {@code SeatHold} expires
     */
    SeatHold(final int numSeats, final Duration seatHoldExpirationTime) {
        this.numSeats = numSeats;
        id = IdGenerator.generateUniqueId(); // should be auto-generated and unique
        expirationTime = Instant.now().plus(seatHoldExpirationTime);
    }

    /**
     * Number of seats held for this instance.
     *
     * @return number of seats held
     */
    public int getNumSeats() {
        return numSeats;
    }

    /**
     * Unique integer ID for the seat hold
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
        numSeats = 0;
    }
}
