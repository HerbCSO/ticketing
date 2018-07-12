package org.dreesbach.ticketing;

import java.util.ArrayList;
import java.util.List;

/**
 * A seat picking strategy for a simple seating arrangement.
 */
public class SimpleSeatingArrangementSimpleSeatPickingStrategy implements SeatPickingStrategy<SimpleSeatingArrangement> {
    /**
     * Go through the available seats and return the best ones.
     *
     * TODO: simplest possible initial implementation for now, this is stupidly inefficient, does not return the best seats
     * yet (only first available), and should be redesigned to work much faster - maybe using a priority queue sorted by best
     * to worst seat?
     *
     * @param numSeatsToPick number of seats to pick for reservation
     * @return an array of available {@link Seat}s in the best locations
     */
    @Override
    public final List<Seat> pickBestAvailableSeats(final SimpleSeatingArrangement seatingArrangement,
                                                   final int numSeatsToPick) {
        int seatsPicked = 0;
        List<Seat> bestSeats = new ArrayList<>();
        Seat[][] seats = seatingArrangement.getSeats();
        for (int row = 0; row < seats.length; row++) {
            for (int col = 0; col < seats[row].length; col++) {
                if (seats[row][col].isAvailable()) {
                    seatsPicked++;
                    bestSeats.add(seats[row][col]);
                    if (seatsPicked == numSeatsToPick) {
                        return bestSeats;
                    }
                }
            }
        }
        return bestSeats;
    }
}
