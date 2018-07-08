package org.dreesbach.ticketing;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimpleSeatingArrangementTest {

    /**
     * Temporarily test this method that should be private, until this is moved into a seat selection strategy
     */
    @Test
    void pickBestAvailableSeats() {
        SeatingArrangement seatingArrangement = new SimpleSeatingArrangement(3, 3);
        List<Seat> seatsPicked = ((SimpleSeatingArrangement) seatingArrangement).pickBestAvailableSeats(2);
        assertEquals("Row 1 Seat 1", seatsPicked.get(0).getId());
        assertEquals("Row 1 Seat 2", seatsPicked.get(1).getId());
    }
}
