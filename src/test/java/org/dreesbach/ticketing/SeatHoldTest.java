package org.dreesbach.ticketing;

import org.dreesbach.ticketing.id.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeatHoldTest {

    private SimpleSeatingArrangement seatingArrangement;

    @BeforeEach
    void setUp() {
        SeatPickingStrategy<SimpleSeatingArrangement> seatPickingStrategy =
                new SimpleSeatingArrangementSimpleSeatPickingStrategy();
        seatingArrangement = new SimpleSeatingArrangement(3, 3, seatPickingStrategy);
    }

    @Test
    void getNumSeats() {
        int numSeats = 2;
        SeatHold seatHold = new SeatHold(2, seatingArrangement);
        assertEquals(numSeats, seatHold.getNumSeatsHeld(), "Number of seats held should equal requested seats");
    }

    @Test
    void requestMoreSeatsThanAvailable() {
        SeatHold seatHold = new SeatHold(1_000_000, seatingArrangement);
        assertEquals(
                seatingArrangement.getTotalNumSeats(),
                seatHold.getNumSeatsHeld(),
                "Number of seats held should equal " + "total number of seats in the location"
        );
    }

    @Test
    void getId() {
        SeatHold seatHold = new SeatHold(2, seatingArrangement);
        assertThat("Generated ID should be > 0", seatHold.getId(), greaterThan(0));
    }

    @Test
    void expired() {
        SeatHold seatHold = new SeatHold(2, seatingArrangement, Duration.ZERO);
        assertTrue(seatHold.expired(), "SeatHold should be expired immediately");
    }

    @Test
    void remove() {
        SeatHold seatHold = new SeatHold(2, seatingArrangement);
        int id = seatHold.getId();
        seatHold.remove();
        assertTrue(seatHold.expired(), "Removed SeatHold should be expired immediately");
        assertEquals(0, seatHold.getNumSeatsHeld(), "SeatHold should have 0 seats associated");
        assertFalse(IdGenerator.retireId(id), "IdGenerator should have the SeatHold's ID retired");
    }
}
