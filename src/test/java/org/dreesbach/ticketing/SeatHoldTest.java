package org.dreesbach.ticketing;

import org.dreesbach.ticketing.id.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeatHoldTest {

    private static final List<Seat> SEATS_TO_HOLD = Arrays.asList(new SeatImpl[]{
            new SeatImpl("seat1", 1.0), new SeatImpl("seat2", 2.0)
    });
    private RectangularVenue venue;

    @BeforeEach
    void setUp() {
        SeatPickingStrategy<RectangularVenue> seatPickingStrategy = new RectangularVenueSimpleSeatPickingStrategy();
        venue = new RectangularVenue(3, 3, seatPickingStrategy);
    }

    @Test
    void getNumSeats() {
        int numSeatsToRequest = 2;
        SeatHold seatHold = new SeatHold(SEATS_TO_HOLD);
        assertEquals(numSeatsToRequest, seatHold.getNumSeatsHeld(), "Number of seats held should equal requested seats");
    }

    @Test
    void testHoldingZeroSeats() {
        SeatHold seatHold = new SeatHold(Collections.emptyList());
        assertEquals(0, seatHold.getNumSeatsRequested(), "Number of seats held should equal requested seats (0)");
    }

    @Test
    void testHoldingNegativeSeats() {
        Throwable exception =
                assertThrows(IllegalArgumentException.class, () -> venue.holdSeats(-1, Duration.ZERO), "Exception expected");
        assertEquals("numSeatsToHold must be > 0", exception.getMessage(), "Wrong exception message");
    }

    @Test
    void requestMoreSeatsThanAvailable() {
        SeatHold seatHold = new SeatHold(venue.getSeats());
        assertEquals(
                venue.getTotalNumSeats(),
                seatHold.getNumSeatsHeld(),
                "Number of seats held should equal " + "total number of seats in the location"
        );
    }

    @Test
    void getId() {
        SeatHold seatHold = venue.holdSeats(2, Duration.ZERO);
        assertThat("Generated ID should be > 0", seatHold.getId(), greaterThan(0));
    }

    @Test
    void expired() throws InterruptedException {
        SeatHold seatHold = venue.holdSeats(2, Duration.ZERO);
        Thread.sleep(10L); // the test fails intermittently without this, since the expiration happens in a separate thread
        assertTrue(seatHold.expired(), "SeatHold should be expired immediately");
    }

    @Test
    void remove() {
        SeatHold seatHold = venue.holdSeats(2, Duration.ZERO);
        int id = seatHold.getId();
        seatHold.remove();
        assertAll(
                "Check SeatHolds",
                () -> assertTrue(seatHold.expired(), "Removed SeatHold should be expired immediately"),
                () -> assertEquals(0, seatHold.getNumSeatsHeld(), "SeatHold should have 0 seats associated"),
                () -> assertFalse(IdGenerator.retireId(id), "IdGenerator should have the SeatHold's ID retired")
        );
    }
}
