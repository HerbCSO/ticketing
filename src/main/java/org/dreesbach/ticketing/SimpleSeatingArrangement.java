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
     * Creates a new instance.
     *
     * @param numRows number of seat rows in this location
     * @param seatsPerRow number of seats per row - constant in this simple arrangement
     */
    SimpleSeatingArrangement(final int numRows, final int seatsPerRow) {
        this.numRows = numRows;
        this.seatsPerRow = seatsPerRow;
    }

    /**
     * Total number of seats - calculated from rows * seats per row.
     *
     * @return total number of seats
     */
    public int getNumSeats() {
        return numRows * seatsPerRow;
    }
}
