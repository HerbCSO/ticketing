package org.dreesbach.ticketing;

import com.google.common.collect.ImmutableList;
import org.dreesbach.ticketing.id.IdGenerator;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class to coordinate holding of seats prior to actually reserving.
 */
class SeatHold {
    /** How long the seat hold is kept before expiring. */
    private static final Duration SEAT_HOLD_EXPIRATION_TIME = Duration.ofMinutes(5);
    /** The location in which seats are being held. */
    private final Venue venue;
    /** The expiration time of this {@code SeatHold}. */
    private Instant expirationTime;
    /** Number of seats to hold for reservation. */
    private int numSeatsRequested;
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
     * @param venue the location to hold seats in
     */
    SeatHold(final int numSeatsRequested, final Venue venue) {
        this(numSeatsRequested, venue, SEAT_HOLD_EXPIRATION_TIME);
    }

    /**
     * Create a new SeatHold with the specified expiration time.
     *
     * @param numSeatsRequested number of seats to hold
     * @param venue the location to hold seats in
     * @param seatHoldExpirationTime duration until the {@code SeatHold} expires
     */
    SeatHold(final int numSeatsRequested, final Venue venue, final Duration seatHoldExpirationTime) {
        checkArgument(numSeatsRequested >= 0, "numSeatsRequested must be > 0");
        checkNotNull(venue, "venue cannot not be null");
        checkNotNull(seatHoldExpirationTime, "seatHoldExpirationTime cannot be null");
        this.numSeatsRequested = numSeatsRequested;
        // TODO: Do I really need to hold on to this here?
        this.venue = venue;
        id = IdGenerator.generateUniqueIntId();
        expirationTime = Instant.now().plus(seatHoldExpirationTime);
        // TODO: This was possibly a stupid decision - maybe I should invert the relationship and make SeatHold be instantiated
        // by Venue instead?
        seatsHeld = this.venue.holdSeats(numSeatsRequested);
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
     * @return a list of @{link Seat}s
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
