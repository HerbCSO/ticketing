package org.dreesbach.ticketing;

import java.util.List;

/**
 * Provides access to various Venue implementations. Can take into account various location arrangements and
 * could be extended to provide various "best seats" algorithms.
 */
public interface Venue {
    /**
     * Return the total number of seats in the theatre.
     *
     * @return total number of seats
     */
    int getTotalNumSeats();

    /**
     * Return the available number of seats in the theatre.
     *
     * @return available number of seats
     */
    int getAvailableNumSeats();

    /**
     * Hold a desired number of seats. Returns best available selection.
     *
     * @param numSeatsToHold the number of seats desired
     * @return number of seats available to hold - may be 0 if none were available
     */
    List<Seat> holdSeats(int numSeatsToHold);

    /**
     * Set a strategy for selecting the best seats.
     *
     * @param seatPickingStrategy the strategy to use
     */
    void setSeatPickingStrategy(SeatPickingStrategy seatPickingStrategy);
}
