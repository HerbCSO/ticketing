package org.dreesbach.ticketing;

/**
 * A simple {@see SeatingArrangement} implementation that provides a rectangular arrangement of seats.
 */
final class SimpleSeatingArrangement implements SeatingArrangement {
    /**
     * Number of rows in the location.
     */
    private final int numRows;
    /**
     * Number of seats per row.
     */
    private final int seatsPerRow;
    /**
     * Number of seats left available.
     */
    private int availableNumSeats;

    /**
     * Creates a new instance.
     *
     * @param numRows number of seat rows in this location
     * @param seatsPerRow number of seats per row - same for all rows in this simple arrangement
     */
    SimpleSeatingArrangement(final int numRows, final int seatsPerRow) {
        this.numRows = numRows;
        this.seatsPerRow = seatsPerRow;
        availableNumSeats = getTotalNumSeats();
    }

    /**
     * Total number of seats - calculated from rows * seats per row.
     *
     * @return total number of seats
     */
    @Override
    public int getTotalNumSeats() {
        return numRows * seatsPerRow;
    }

    @Override
    public synchronized int getAvailableNumSeats() {
        return availableNumSeats;
    }

    /**
     * Reserve seats in the location.
     *
     * This method is synchronized to ensure only one thread at a time can reserve seats. This may end up being a bottleneck
     * later on, something to watch out for in a multi-threaded web server environment, for example.
     *
     * @param numSeatsToReserve the number of seats to be reserved
     * @return the actual number of seats that could be reserved - could be less than what was requested, all the way down to 0
     */
    public synchronized int reserveSeats(final int numSeatsToReserve) {
        int reservedSeats;
        if (availableNumSeats - numSeatsToReserve < 0) {
            reservedSeats = availableNumSeats;
        } else {
            reservedSeats = numSeatsToReserve;
        }
        availableNumSeats = availableNumSeats - reservedSeats;
        return reservedSeats;
    }
}
