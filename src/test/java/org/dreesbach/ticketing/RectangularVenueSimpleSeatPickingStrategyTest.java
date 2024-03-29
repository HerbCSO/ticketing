package org.dreesbach.ticketing;

import org.dreesbach.ticketing.id.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        TestUtil.testException(
                IllegalArgumentException.class,
                () -> new RectangularVenue(-1, 0, seatPickingStrategy),
                "Number of rows must be > 0"
        );
    }

    @Test
    void checkZeroRowNumberThrowsException() {
        TestUtil.testException(
                IllegalArgumentException.class,
                () -> new RectangularVenue(0, 0, seatPickingStrategy),
                "Number of rows must be > 0"
        );
    }

    @Test
    void checkNegativeColumnNumberThrowsException() {
        TestUtil.testException(
                IllegalArgumentException.class,
                () -> new RectangularVenue(1, -1, seatPickingStrategy),
                "Number of seats per row must be > 0"
        );
    }

    @Test
    void checkZeroColumnNumberThrowsException() {
        TestUtil.testException(
                IllegalArgumentException.class,
                () -> new RectangularVenue(1, 0, seatPickingStrategy),
                "Number of seats per row must be > 0"
        );
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
        TestUtil.testException(
                IllegalArgumentException.class,
                () -> seatPickingStrategy.pickBestAvailableSeats(venue, -1),
                "Number of seats to pick must be greater than 0"
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
            "1, 0, '0.0'",
            "10, 9, '-4.5'",
            "2, 0, '0.5'",
            "2, 1, '-0.5'",
            "5, 0, '2.0'",
            "5, 1, '1.0'",
            "5, 2, '0.0'",
            "5, 3, '-1.0'",
            "5, 4, '-2.0'",
            "4, 0, '1.5'",
            "4, 1, '0.5'",
            "4, 2, '-0.5'",
            "4, 3, '-1.5'"
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
            "2, 2, 0, 0, 0.25",
            "2, 2, 0, 1, 0.25",
            "2, 2, 1, 0, 1.25",
            "2, 2, 1, 1, 1.25",
            "3, 3, 0, 0, 1.0",
            "3, 3, 0, 1, 0.0",
            "3, 3, 0, 2, 1.0",
            "3, 3, 1, 0, 2.0",
            "3, 3, 1, 1, 1.0",
            "3, 3, 1, 2, 2.0",
            "3, 3, 2, 0, 5.0",
            "3, 3, 2, 1, 4.0",
            "3, 3, 2, 2, 5.0",
            "4, 4, 0, 0, 2.25",
            "4, 4, 0, 1, 0.25",
            "4, 4, 0, 2, 0.25",
            "4, 4, 0, 3, 2.25",
            "4, 4, 1, 0, 3.25",
            "4, 4, 1, 1, 1.25",
            "4, 4, 1, 2, 1.25",
            "4, 4, 1, 3, 3.25",
            "4, 4, 2, 0, 6.25",
            "4, 4, 2, 1, 4.25",
            "4, 4, 2, 2, 4.25",
            "4, 4, 2, 3, 6.25",
            "4, 4, 3, 0, 11.25",
            "4, 4, 3, 1, 9.25",
            "4, 4, 3, 2, 9.25",
            "4, 4, 3, 3, 11.25",
    })
    void seatGoodness(int rows, int numSeatsPerRow, int row, int column, float expectedGoodness) {
        venue = new RectangularVenue(rows, numSeatsPerRow, seatPickingStrategy);
        assertThat("Goodness score didn't match expected", venue.getGoodness(row, column), closeTo(expectedGoodness, 0.001));
    }

    @Test
    void badGoodnessRow() {
        TestUtil.testException(
                IllegalArgumentException.class,
                () -> venue.getGoodness(-1, 0),
                "row must be between 0 and 2 (inclusive)"
        );
        TestUtil.testException(
                IllegalArgumentException.class,
                () -> venue.getGoodness(1_000_000, 0),
                "row must be between 0 and 2 (inclusive)"
        );
    }

    @Test
    void badGoodnessCol() {
        TestUtil.testException(
                IllegalArgumentException.class,
                () -> venue.getGoodness(0, -1),
                "col must be between 0 and 2 (inclusive)"
        );
        TestUtil.testException(
                IllegalArgumentException.class,
                () -> venue.getGoodness(0, 1_000_000),
                "col must be between 0 and 2 (inclusive)"
        );
    }

    @Test
    void holdNegativeNumberOfSeatsThrowsException() {
        TestUtil.testException(
                IllegalArgumentException.class,
                () -> venue.holdSeats(-1, Duration.ZERO),
                "numSeatsToHold must be > 0"
        );
    }

    @Test
    void displaySeatHoldProgress() throws NoSuchAlgorithmException {
        List<String> reservationIds = new ArrayList<>();
        SecureRandom rnd = SecureRandom.getInstanceStrong();
        rnd.setSeed(Instant.now().getEpochSecond());
        int numRows = 9, numCols = 11;
        venue = new RectangularVenue(numRows, numCols, seatPickingStrategy);
        List<SeatHold> seatHolds = new ArrayList<>();
        for (int i = 0; i < numRows * numCols; i++) {
            seatHolds.add(venue.holdSeats(rnd.nextInt(4) + 1, Duration.ofSeconds(2)));
            if (rnd.nextFloat() > 0.1 && !seatHolds.isEmpty()) {
                final int index = rnd.nextInt(seatHolds.size());
                reservationIds.add(venue.reserve(seatHolds.get(index)));
                seatHolds.remove(index);
            }
            if (rnd.nextFloat() > 0.95 && !reservationIds.isEmpty()) {
                final int index = rnd.nextInt(reservationIds.size());
                venue.cancelReservation(reservationIds.get(index));
                reservationIds.remove(index);
            }
            venue.printSeats();
        }
    }

    @Test
    void reserveSeat() {
        int numSeatsToReserve = 2;
        SeatHold seatHold = venue.holdSeats(numSeatsToReserve, Duration.ZERO);
        String reservationCode = venue.reserve(seatHold);
        assertAll("Check postconditions", () -> assertEquals(venue.getTotalNumSeats() - numSeatsToReserve,
                venue.getAvailableNumSeats(),
                "Should have reserved " + numSeatsToReserve + " seats"
        ), () -> assertEquals(6, reservationCode.length(), "Expected a 6 character reservation code"));
    }

    @Test
    void reservationCodeLength() {
        TestUtil.testException(
                IllegalArgumentException.class,
                () -> venue.cancelReservation("BAD"),
                "Expected a " + IdGenerator.MAX_RESERVATION_CODE_LENGTH + "-character reservation code"
        );
    }

    @Test
    void cancelReservation() {
        int numSeatsToReserve = 2;
        SeatHold seatHold = venue.holdSeats(numSeatsToReserve, Duration.ZERO);
        String reservationCode = venue.reserve(seatHold);
        venue.cancelReservation(reservationCode);
        assertEquals(venue.getTotalNumSeats(), venue.getAvailableNumSeats(), "All seats should be available again");
        TestUtil.testException(
                IllegalArgumentException.class,
                () -> venue.cancelReservation(reservationCode),
                "Reservation code " + reservationCode + " not found"
        );
    }

    @Test
    void cancelledReservationIsAvailableForHoldAgain() {
        int numSeatsToReserve = 2;
        SeatHold seatHold = venue.holdSeats(numSeatsToReserve, Duration.ZERO);
        String reservationCode = venue.reserve(seatHold);
        venue.cancelReservation(reservationCode);
        assertTrue(seatHold.getSeatsHeld().stream().allMatch(Seat::isAvailable), "Not all seats are available");
    }

    @Test
    void seatListOnlyPopulatedOnce() {
        venue.populateSeatList();
        TestUtil.testException(
                IllegalStateException.class,
                () -> venue.populateSeatList(),
                "seatList was already populated"
        );
    }
}
