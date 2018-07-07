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
    int getNumSeats();
}
