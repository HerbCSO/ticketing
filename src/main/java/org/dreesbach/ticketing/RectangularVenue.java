package org.dreesbach.ticketing;

import java.util.List;

/**
 * A simple {@link Venue} implementation that provides a rectangular arrangement of seats.
 */
final class RectangularVenue implements Venue {
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
     * A two-dimensional array for the seats at the venue. A simplistic implementation for this simplistic seating setup.
     */
    private Seat[][] seats;
    /**
     * The seat picking strategy to use.
     */
    private SeatPickingStrategy seatPickingStrategy;

    /**
     * Creates a new instance.
     *
     * @param numRows number of seat rows in this location
     * @param seatsPerRow number of seats per row - same for all rows in this simple arrangement
     * @param seatPickingStrategy the strategy for picking the best seats
     */
    RectangularVenue(final int numRows, final int seatsPerRow,
                     final SeatPickingStrategy<RectangularVenue> seatPickingStrategy) {
        this.numRows = numRows;
        this.seatsPerRow = seatsPerRow;
        availableNumSeats = getTotalNumSeats();
        seats = new Seat[numRows][seatsPerRow];
        fillSeats(seats);
        setSeatPickingStrategy(seatPickingStrategy);
    }

    /**
     * Fill the seats array with default {@link Seat} instances.
     *
     * @param seats the seats array
     */
    private void fillSeats(final Seat[][] seats) {
        for (int row = 0; row < seats.length; row++) {
            for (int col = 0; col < seats[row].length; col++) {
                // we set seat IDs to be 1-indexed for normal human consumption
                seats[row][col] = new SeatImpl(String.format("Row %d Seat %d", row + 1, col + 1));
                // TODO: set seat "value" to identify best to worst seats here
            }
        }
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
     * <p>
     * This method is synchronized to ensure only one thread at a time can hold seats. This may end up being a bottleneck
     * later on, something to watch out for in a multi-threaded web server environment, for example.
     *
     * @param numSeatsToHold the number of seats to be reserved
     * @return the actual number of seats that could be reserved - could be less than what was requested, all the way down to 0
     */
    public synchronized List<Seat> holdSeats(final int numSeatsToHold) {
        List<Seat> bestSeats = seatPickingStrategy.pickBestAvailableSeats(this, numSeatsToHold);
        for (Seat seat : bestSeats) {
            seat.hold();
        }
        availableNumSeats = availableNumSeats - bestSeats.size();
        return bestSeats;
    }

    @Override
    public void setSeatPickingStrategy(final SeatPickingStrategy seatPickingStrategy) {
        this.seatPickingStrategy = seatPickingStrategy;
    }

    /**
     * Get the seats for this location.
     *
     * @return two-dimensional array of seats by row/column
     */
    public Seat[][] getSeats() {
        return seats;
    }
}
