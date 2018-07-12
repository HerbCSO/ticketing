package org.dreesbach.ticketing;

import org.dreesbach.ticketing.id.IdGenerator;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Class to coordinate holding of seats prior to actually reserving.
 */
class SeatHold {
    /** How long the seat hold is kept before expiring. */
    private static final Duration SEAT_HOLD_EXPIRATION_TIME = Duration.ofMinutes(5);
    /** The location in which seats are being held. */
    private final SeatingArrangement seatingArrangement;
    /** The expiration time of this {@code SeatHold}. */
    private Instant expirationTime;
    /** Number of seats to hold for reservation. */
    private int numSeatsRequested;
    /** The number of seats actually held. */
    private int numSeatsHeld;
    /** Unique ID of the seat hold. */
    private int id;
    /** List of seats held. */
    private List<Seat> seatsHeld;

    /**
     * Create a new SeatHold.
     *
     * Will auto-generate an ID.
     *
     * @param numSeatsRequested number of seats to hold
     * @param seatingArrangement the location to hold seats in
     */
    SeatHold(final int numSeatsRequested, final SeatingArrangement seatingArrangement) {
        this(numSeatsRequested, seatingArrangement, SEAT_HOLD_EXPIRATION_TIME);
    }

    /**
     * Create a new SeatHold with the specified expiration time.
     *
     * @param numSeatsRequested number of seats to hold
     * @param seatingArrangement the location to hold seats in
     * @param seatHoldExpirationTime duration until the {@code SeatHold} expires
     */
    SeatHold(final int numSeatsRequested, final SeatingArrangement seatingArrangement, final Duration seatHoldExpirationTime) {
        this.numSeatsRequested = numSeatsRequested;
        // TODO: Do I really need to hold on to this here?
        this.seatingArrangement = seatingArrangement;
        id = IdGenerator.generateUniqueId();
        expirationTime = Instant.now().plus(seatHoldExpirationTime);
        // TODO: This was possibly a stupid decision - maybe I should invert the relationship and make SeatHold be instantiated
        // by SeatingArrangement instead?
        seatsHeld = this.seatingArrangement.holdSeats(numSeatsRequested);
        numSeatsHeld = seatsHeld.size();
    }

    /**
     * Number of seats held for this instance.
     *
     * @return number of seats held
     */
    public int getNumSeatsHeld() {
        return numSeatsHeld;
    }

    /**
     * Get the actual seats held by this.
     *
     * @return a list of @{link Seat}s
     */
    public List<Seat> getSeatsHeld() {
        return Collections.unmodifiableList(seatsHeld);
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
        numSeatsHeld = 0;
        seatsHeld = null;
    }
}
