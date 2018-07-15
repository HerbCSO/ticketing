package org.dreesbach.ticketing;

import org.dreesbach.ticketing.id.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RectangularVenueSimpleSeatPickingStrategyTest {

    private SeatPickingStrategy<RectangularVenue> seatPickingStrategy;
    private RectangularVenue venue;

    @BeforeEach
    void setup() {
        seatPickingStrategy = new RectangularVenueSimpleSeatPickingStrategy();
        venue = new RectangularVenue(3, 3, seatPickingStrategy);
    }

    @Test
    void checkNegativeRowNumberThrowsException() {
        Throwable exception = assertThrows(IllegalArgumentException.class,
                () -> new RectangularVenue(-1, 0, seatPickingStrategy),
                "Expected negative row number to throw exception"
        );
        assertEquals("Number of rows must be > 0", exception.getMessage(), "Wrong exception message");
    }

    @Test
    void checkZeroRowNumberThrowsException() {
        Throwable exception = assertThrows(IllegalArgumentException.class,
                () -> new RectangularVenue(0, 0, seatPickingStrategy),
                "Expected zero row number to throw exception"
        );
        assertEquals("Number of rows must be > 0", exception.getMessage(), "Wrong exception message");
    }

    @Test
    void checkNegativeColumnNumberThrowsException() {
        Throwable exception = assertThrows(IllegalArgumentException.class,
                () -> new RectangularVenue(1, -1, seatPickingStrategy),
                "Expected negative column number to throw exception"
        );
        assertEquals("Number of seats per row must be > 0", exception.getMessage(), "Wrong exception message");
    }

    @Test
    void checkZeroColumnNumberThrowsException() {
        Throwable exception = assertThrows(IllegalArgumentException.class,
                () -> new RectangularVenue(1, 0, seatPickingStrategy),
                "Expected zero column number to throw exception"
        );
        assertEquals("Number of seats per row must be > 0", exception.getMessage(), "Wrong exception message");
    }

    @Test
    void pickBestAvailableSeats() {
        List<Seat> seatsPicked = seatPickingStrategy.pickBestAvailableSeats(venue, 2);
        assertAll("check correct seats got picked",
                () -> assertEquals("Row 1 Seat 2", seatsPicked.get(0).getId()),
                () -> assertEquals("Row 1 Seat 1", seatsPicked.get(1).getId())
        );
    }

    @Test
    void pickAllAvailableSeats() {
        final int numSeatsToPick = 9;
        List<Seat> seatsPicked = seatPickingStrategy.pickBestAvailableSeats(venue, numSeatsToPick);
        assertEquals(numSeatsToPick, seatsPicked.size(), "Expected all 9 seats to get picked");
    }

    @Test
    void pickNegativeNumberOfSeatsThrowsException() {
        Throwable exception =
                assertThrows(IllegalArgumentException.class, () -> seatPickingStrategy.pickBestAvailableSeats(venue, -1));
        assertEquals(exception.getMessage(),
                "Number of seats to pick must be greater than 0",
                "Excpetion message didn't match"
        );
    }

    @Test
    void multiplePicksEmptyOutAllAvailableSeats() {
        List<Seat> seatsPicked = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            seatsPicked.addAll(seatPickingStrategy.pickBestAvailableSeats(venue, 1));
        }
        assertEquals(9, seatsPicked.size(), "Expected all 9 seats to get picked and no more");
    }

    @DisplayName("Row positions")
    @ParameterizedTest(name = "Row [{1}] out of [{0}] should have a score of [{2}]")
    @CsvSource({ "1, 0, 0", "10, 9, 9", "5, 0, 0", "5, 1, 1", "5, 2, 2", "5, 3, 3", "5, 4, 4" })
    void yPosition(int numRowsInVenue, int row, int expectedYPosition) {
        venue = new RectangularVenue(numRowsInVenue, 1, seatPickingStrategy);
        assertEquals(expectedYPosition, venue.getYPosition(row), "Y position didn't match expected");
    }

    @Test
    void outOfRangeRowThrowsException() {
        assertAll("range checking",
                () -> assertThrows(IllegalArgumentException.class,
                        () -> venue.getYPosition(-1),
                        "Too low a row number throws exception"
                ),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> venue.getYPosition(10),
                        "Too high a row number throws exception"
                )
        );
    }

    @DisplayName("Column goodness scores")
    @ParameterizedTest(name = "Column [{1}] out of [{0}] should have a score of [{2}]")
    @CsvSource({
            "1, 0, '-0.0'",
            "10, 9, '4.5'",
            "2, 0, '-0.5'",
            "2, 1, '0.5'",
            "5, 0, '-2.0'",
            "5, 1, '-1.0'",
            "5, 2, '-0.0'",
            "5, 3, '1.0'",
            "5, 4, '2.0'",
            "4, 0, '-1.5'",
            "4, 1, '-0.5'",
            "4, 2, '0.5'",
            "4, 3, '1.5'"
    })
    void xPosition(int numSeatsPerRow, int column, float expectedXPosition) {
        venue = new RectangularVenue(1, numSeatsPerRow, seatPickingStrategy);
        assertEquals(expectedXPosition, venue.getXPosition(column), "X position didn't match expected");
    }

    @Test
    void outOfRangeColumnThrowsException() {
        assertAll(
                "range checking",
                () -> assertThrows(IllegalArgumentException.class,
                        () -> venue.getXPosition(-1),
                        "Too low a column number throws exception"
                ),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> venue.getXPosition(10),
                        "Too high a column number throws exception"
                )
        );
    }

    @DisplayName("Column goodness scores")
    @ParameterizedTest(name = "Seat [{2}, {3}] out of [{0}, {1}] should have goodness score of [{4}]")
    @CsvSource({
            "1, 1, 0, 0, 0.0",
            "2, 2, 0, 0, 0.5",
            "2, 2, 0, 1, 0.5",
            "2, 2, 1, 0, 1.118033988749895",
            "2, 2, 1, 1, 1.118033988749895",
            "3, 3, 0, 0, 1.0",
            "3, 3, 0, 1, 0.0",
            "3, 3, 0, 2, 1.0",
            "3, 3, 1, 0, 1.4142135623730951",
            "3, 3, 1, 1, 1.0",
            "3, 3, 1, 2, 1.4142135623730951",
            "3, 3, 2, 0, 2.23606797749979",
            "3, 3, 2, 1, 2.0",
            "3, 3, 2, 2, 2.23606797749979",
            "4, 4, 0, 0, 1.5",
            "4, 4, 0, 1, 0.5",
            "4, 4, 0, 2, 0.5",
            "4, 4, 0, 3, 1.5",
            "4, 4, 1, 0, 1.8027756377319946",
            "4, 4, 1, 1, 1.118033988749895",
            "4, 4, 1, 2, 1.118033988749895",
            "4, 4, 1, 3, 1.8027756377319946",
            "4, 4, 2, 0, 2.5",
            "4, 4, 2, 1, 2.0615528128088303",
            "4, 4, 2, 2, 2.0615528128088303",
            "4, 4, 2, 3, 2.5",
            "4, 4, 3, 0, 3.3541019662496847",
            "4, 4, 3, 1, 3.0413812651491097",
            "4, 4, 3, 2, 3.0413812651491097",
            "4, 4, 3, 3, 3.3541019662496847",
    })
    void seatGoodness(int rows, int numSeatsPerRow, int row, int column, float expectedGoodness) {
        venue = new RectangularVenue(rows, numSeatsPerRow, seatPickingStrategy);
        assertThat("Goodness score didn't match expected", venue.getGoodness(row, column), closeTo(expectedGoodness, 0.001));
    }

    @Test
    void badGoodnessRow() {
        assertThrows(IllegalArgumentException.class,
                () -> venue.getGoodness(-1, 0),
                "Expected negative row number to throw exception"
        );
        assertThrows(IllegalArgumentException.class,
                () -> venue.getGoodness(1_000_000, 0),
                "Expected too large row number to throw exception"
        );
    }

    @Test
    void badGoodnessCol() {
        assertThrows(IllegalArgumentException.class,
                () -> venue.getGoodness(0, -1),
                "Expected negative column number to throw exception"
        );
        assertThrows(IllegalArgumentException.class,
                () -> venue.getGoodness(0, 1_000_000),
                "Expected too large column number to throw exception"
        );
    }

    @Test
    void holdNegativeNumberOfSeatsThrowsException() {
        Throwable exception =
                assertThrows(IllegalArgumentException.class, () -> venue.holdSeats(-1), "Expected exception " + "thrown");
        assertEquals("numSeatsToHold must be >= 0", exception.getMessage(), "Wrong exception message");
    }

    @Test
    void displaySeatHoldProgress() {
        venue = new RectangularVenue(10, 30, seatPickingStrategy);
        for (int i = 0; i < 300; i++) {
            venue.holdSeats(1);
            venue.printSeats();
        }
    }

    @Test
    void reserveSeat() {
        int numSeatsToReserve = 2;
        SeatHold seatHold = new SeatHold(numSeatsToReserve, venue);
        String reservationCode = venue.reserve(seatHold);
        assertAll("Check postconditions", () -> assertEquals(venue.getTotalNumSeats() - numSeatsToReserve,
                venue.getAvailableNumSeats(),
                "Should have reserved " + numSeatsToReserve + " seats"
        ), () -> assertEquals(6, reservationCode.length(), "Expected a 6 character reservation code"));
    }

    @Test
    void reservationCodeLength() {
        Throwable exception =
                assertThrows(IllegalArgumentException.class, () -> venue.cancelReservation("BAD"), "Expected exception thrown");
        assertEquals(
                "Expected a " + IdGenerator.MAX_RESERVATION_CODE_LENGTH + "-character reservation code",
                exception.getMessage(),
                "Wrong exception message"
        );
    }

    @Test
    void cancelReservation() {
        int numSeatsToReserve = 2;
        SeatHold seatHold = new SeatHold(numSeatsToReserve, venue);
        String reservationCode = venue.reserve(seatHold);
        venue.cancelReservation(reservationCode);
        assertEquals(venue.getTotalNumSeats(), venue.getAvailableNumSeats(), "All seats should be available again");
        assertThrows(IllegalArgumentException.class,
                () -> venue.cancelReservation(reservationCode),
                "Venue should no longer know about this reservation code"
        );
    }
}
