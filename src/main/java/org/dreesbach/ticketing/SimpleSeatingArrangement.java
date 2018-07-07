package org.dreesbach.ticketing;

public class SimpleSeatingArrangement implements SeatingArrangement {
    private final int numRows;
    private final int seatsPerRow;
    private int numSeats;

    public SimpleSeatingArrangement(int numRows, int seatsPerRow) {
        this.numRows = numRows;
        this.seatsPerRow = seatsPerRow;
        this.numSeats = numRows * seatsPerRow;
    }

    public int getNumSeats() {
        return numSeats;
    }
}
