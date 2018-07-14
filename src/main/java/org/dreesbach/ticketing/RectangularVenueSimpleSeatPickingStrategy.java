package org.dreesbach.ticketing;

import java.util.ArrayList;
import java.util.List;

/**
 * A seat picking strategy for a retangular venue.
 */
public class RectangularVenueSimpleSeatPickingStrategy implements SeatPickingStrategy<RectangularVenue> {
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
    public final List<Seat> pickBestAvailableSeats(final RectangularVenue venue,
                                                   final int numSeatsToPick) {
        int seatsPicked = 0;
        List<Seat> bestSeats = new ArrayList<>();
        Seat[][] seats = venue.getSeats();
        for (int row = 0; row < seats.length; row++) {
            for (int col = 0; col < seats[row].length; col++) {
                if (seats[row][col].isAvailable()) {
                    if (seatsPicked == numSeatsToPick) {
                        return bestSeats;
                    }
                    seatsPicked++;
                    bestSeats.add(seats[row][col]);
                }
            }
        }
        return bestSeats;
    }
}
