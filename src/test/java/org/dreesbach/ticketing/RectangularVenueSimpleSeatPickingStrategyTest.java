package org.dreesbach.ticketing;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RectangularVenueSimpleSeatPickingStrategyTest {

    @Test
    void pickBestAvailableSeats() {
        SeatPickingStrategy<RectangularVenue> seatPickingStrategy =
                new RectangularVenueSimpleSeatPickingStrategy();
        RectangularVenue venue = new RectangularVenue(3, 3, seatPickingStrategy);
        List<Seat> seatsPicked = seatPickingStrategy.pickBestAvailableSeats(venue, 2);
        assertEquals("Row 1 Seat 1", seatsPicked.get(0).getId());
        assertEquals("Row 1 Seat 2", seatsPicked.get(1).getId());
    }
}
