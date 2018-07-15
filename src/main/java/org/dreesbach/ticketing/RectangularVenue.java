package org.dreesbach.ticketing;

import org.dreesbach.ticketing.id.IdGenerator;

import java.util.Collections;
import java.util.List;

import static java.lang.Math.sqrt;

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
    RectangularVenue(
            final int numRows, final int seatsPerRow, final SeatPickingStrategy<RectangularVenue> seatPickingStrategy
    ) {
        if (numRows < 1) {
            throw new IllegalArgumentException("Number of rows must be > 0");
        }
        if (seatsPerRow < 1) {
            throw new IllegalArgumentException("Number of seats per row must be > 0");
        }
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
                seats[row][col] = new SeatImpl(String.format("Row %d Seat %d", row + 1, col + 1), getGoodness(row, col));
            }
        }
    }

    /**
     * Get the overall "goodness" score of the seat. The assumption here is that the closer to the front and the closer to the
     * middle, the better the seat. In other words the "goodness" is maximum front row, center seat, and then decreases in
     * concentric shells from there, with preference given to seats further forward.
     *
     * @param row row number of the seat, from 0 to n
     * @param col column number of the seat, from 0 to n
     * @return the "goodness" score - relative to the size of the venue, the higher the better, minimum of 1
     */
    double getGoodness(final int row, final int col) {
        return sqrt(getYPosition(row) * getYPosition(row) + getXPosition(col) * getXPosition(col));
    }

    /**
     * The "goodness" score of the row overall. Ranges from 1 to {@ref numRows}.
     *
     * @param row row number of the seat, from 1 to n
     * @return the "goodness" score - relative to the size of the venue, the lower the better, minimum of 0
     */
    double getYPosition(final int row) {
        if (row < 0 || row >= numRows) {
            throw new IllegalArgumentException("Row must be between 0 and " + (numRows - 1));
        }
        return (double) row;
    }

    /**
     * The "goodness" score of the column. Ranges from 1 to {@ref numRows}.
     *
     * @param col column number of the seat, from 0 to seatsPerRow / 2
     * @return the "goodness" score - relative to the size of the venue, the lower the better, minimum of 0
     */
    double getXPosition(final int col) {
        if (col < 0 || col >= seatsPerRow) {
            throw new IllegalArgumentException("Column must be between 0 and " + (seatsPerRow - 1));
        }
        return -(((double) (seatsPerRow - 1) / 2) - col);
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
    public int getAvailableNumSeats() {
        return availableNumSeats;
    }

    /**
     * Hold seats in the location.
     * <p>
     * This method is synchronized to ensure only one thread at a time can hold seats. This may end up being a bottleneck later
     * on, something to watch out for in a multi-threaded web server environment, for example.
     *
     * @param numSeatsToHold the number of seats to be held
     * @return the actual number of seats that could be held - could be less than what was requested, all the way down to 0
     */
    public synchronized List<Seat> holdSeats(final int numSeatsToHold) {
        List<Seat> bestSeats = seatPickingStrategy.pickBestAvailableSeats(this, numSeatsToHold);
        for (Seat seat : bestSeats) {
            seat.hold();
        }
        availableNumSeats -= bestSeats.size();
        return bestSeats;
    }

    @Override
    public synchronized void removeHold(final SeatHold seatHold) {
        availableNumSeats += seatHold.getNumSeatsHeld();
        seatHold.remove();
    }

    @Override
    public void setSeatPickingStrategy(final SeatPickingStrategy seatPickingStrategy) {
        this.seatPickingStrategy = seatPickingStrategy;
    }

    @Override
    public String reserve(final SeatHold seatHold) {
        for (Seat seat : seatHold.getSeatsHeld()) {
            seat.reserve();
        }
        return IdGenerator.generateReservationCode();
    }

    /**
     * Get the seats for this location.
     *
     * @return two-dimensional array of seats by row/column
     */
    public Seat[][] getSeats() {
        return seats;
    }

    @Override
    public void printSeats() {
        String header = " STAGE ";
        int padding = (seatsPerRow * 2 - header.length()) / 2;
        System.out.println(
                String.join("", Collections.nCopies(padding, "-"))
                + " STAGE "
                + String.join("", Collections.nCopies(padding, "-")
        ));
        for (Seat[] seatRow : seats) {
            for (Seat seat : seatRow) {
                if (seat.isAvailable()) {
                    System.out.print("A ");
                    continue;
                }
                if (seat.isReserved()) {
                    System.out.print("R ");
                    continue;
                }
                if (seat.isHeld()) {
                    System.out.print("H ");
                    continue;
                }
            }
            System.out.println();
        }
    }
}
