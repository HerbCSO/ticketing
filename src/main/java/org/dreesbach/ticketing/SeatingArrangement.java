package org.dreesbach.ticketing;

/**
 * Provides access to various SeatingArrangement implementations. Can take into account various location arrangements and
 * could be extended to provide various "best seats" algorithms.
 */
public interface SeatingArrangement {
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
     * Reserve a desired number of seats. Returns best available selection.
     *
     * @param numSeatsToReserve the number of seats desired
     * @return number of seats available to reserve - may be 0 if none were available
     */
    int reserveSeats(int numSeatsToReserve);
}
