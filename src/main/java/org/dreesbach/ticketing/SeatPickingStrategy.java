package org.dreesbach.ticketing;

import java.util.List;

/**
 * Defines a strategy for selecting the best seats.
 *
 * @param <T> the particular seating arrangement that this strategy applies to
 */
public interface SeatPickingStrategy<T extends SeatingArrangement> {
    /**
     * Pick the best seats for the given {@link SeatingArrangement} and number of seats.
     *
     * @param seatingArrangement the {@link SeatingArrangement} to use
     * @param numSeatsToPick number of seats to pick
     * @return an array of Seats
     */
    List<Seat> pickBestAvailableSeats(T seatingArrangement, int numSeatsToPick);
}
