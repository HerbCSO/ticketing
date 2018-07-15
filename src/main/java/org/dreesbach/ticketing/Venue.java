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
     * Remove a prior SeatHold (e.g. when it expires).
     *
     * @param seatHold the SeatHold to remove
     */
    void removeHold(SeatHold seatHold);

    /**
     * Set a strategy for selecting the best seats.
     *
     * @param seatPickingStrategy the strategy to use
     */
    void setSeatPickingStrategy(SeatPickingStrategy seatPickingStrategy);

    /**
     * Reserve previously-held seat(s).
     *
     * @param seatHold the {@link SeatHold} to make a reservation for
     * @return a reservation code
     */
    String reserve(SeatHold seatHold);

    /**
     * Cancels a reservation.
     *
     * @param reservationCode the unique reservation code to cancel
     */
    void cancelReservation(String reservationCode);

    /**
     * Print out the current status of all seats. Simply a convenience method to be able to quickly see what the venue looks
     * like at this time.
     */
    void printSeats();
}
