package org.dreesbach.ticketing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RectangularVenueSimpleSeatPickingStrategyTest {

    private SeatPickingStrategy<RectangularVenue> seatPickingStrategy;
    private RectangularVenue venue;

    @BeforeEach
    void setup() {
        seatPickingStrategy = new RectangularVenueSimpleSeatPickingStrategy();
        venue = new RectangularVenue(3, 3, seatPickingStrategy);
    }

    @Test
    void pickBestAvailableSeats() {
        List<Seat> seatsPicked = seatPickingStrategy.pickBestAvailableSeats(venue, 2);
        assertAll(
                "check correct seats got picked",
                () -> assertEquals("Row 1 Seat 1", seatsPicked.get(0).getId()),
                () -> assertEquals("Row 1 Seat 2", seatsPicked.get(1).getId())
        );
    }

    @Test
    void pickAllAvailableSeats() {
        final int numSeatsToPick = 9;
        List<Seat> seatsPicked = seatPickingStrategy.pickBestAvailableSeats(venue, numSeatsToPick);
        assertEquals(numSeatsToPick, seatsPicked.size(), "Expected all 9 seats to get picked");
    }

    @Test
    void multiplePicksEmptyOutAllAvailableSeats() {
        List<Seat> seatsPicked = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            seatsPicked.addAll(seatPickingStrategy.pickBestAvailableSeats(venue, 1));
        }
        assertEquals(9, seatsPicked.size(), "Expected all 9 seats to get picked");
    }
}
