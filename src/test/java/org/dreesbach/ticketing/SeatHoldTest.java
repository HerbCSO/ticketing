package org.dreesbach.ticketing;

import org.dreesbach.ticketing.id.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.jupiter.api.Assertions.*;

class SeatHoldTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getNumSeats() {
        int numSeats = 2;
        SeatHold seatHold = new SeatHold(2);
        assertEquals(numSeats, seatHold.getNumSeats(), "Number of seats held should equal requested seats");
    }

    @Test
    void getId() {
        SeatHold seatHold = new SeatHold(2);
        assertThat("Generated ID should be > 0", seatHold.getId(), greaterThan(0));
    }

    @Test
    void expired() throws InterruptedException {
        SeatHold seatHold = new SeatHold(2, Duration.ZERO);
        assertTrue(seatHold.expired(), "SeatHold should be expired immediately");
    }

    @Test
    void remove() {
        SeatHold seatHold = new SeatHold(2);
        int id = seatHold.getId();
        seatHold.remove();
        assertTrue(seatHold.expired(), "Removed SeatHold should be expired immediately");
        assertEquals(0, seatHold.getNumSeats(), "SeatHold should have 0 seats associated");
        assertFalse(IdGenerator.retireId(id), "IdGenerator should have the SeatHold's ID retired");
    }
}
