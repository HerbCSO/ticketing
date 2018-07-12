package org.dreesbach.ticketing;

import java.util.List;

/**
 * Defines a strategy for selecting the best seats.
 *
 * @param <T> the particular venue that this strategy applies to
 */
public interface SeatPickingStrategy<T extends Venue> {
    /**
     * Pick the best seats for the given {@link Venue} and number of seats.
     *
     * @param venue the {@link Venue} to use
     * @param numSeatsToPick number of seats to pick
     * @return an array of Seats
     */
    List<Seat> pickBestAvailableSeats(T venue, int numSeatsToPick);
}
