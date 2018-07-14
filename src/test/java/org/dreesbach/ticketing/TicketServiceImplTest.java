package org.dreesbach.ticketing;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketServiceImplTest {
    private static final String CUSTOMER_EMAIL = "me@you.com";
    private static final int NUM_ROWS = 3;
    private static final int NUM_COLS = 3;
    private Venue defaultVenue;
    private static Venue persistentDefaultVenue;
    private TicketService ticketService;
    private static TicketService persistentTicketService;

    @BeforeAll
    public static void setupAll() {
        SeatPickingStrategy<RectangularVenue> seatPickingStrategy = new RectangularVenueSimpleSeatPickingStrategy();
        persistentDefaultVenue = new RectangularVenue(NUM_ROWS, NUM_COLS, seatPickingStrategy);
        persistentTicketService = new TicketServiceImpl(persistentDefaultVenue);
    }

    @BeforeEach
    public void setup() {
        SeatPickingStrategy<RectangularVenue> seatPickingStrategy = new RectangularVenueSimpleSeatPickingStrategy();
        defaultVenue = new RectangularVenue(NUM_ROWS, NUM_COLS, seatPickingStrategy);
        ticketService = new TicketServiceImpl(defaultVenue);
    }

    @Test
    void allSeatsAvailable() {
        assertEquals(defaultVenue.getTotalNumSeats(),
                ticketService.numSeatsAvailable(),
                "Should have all seats " + "available"
        );
    }

    @Test
    void findAndHoldSeats() {
        int numSeatsToHold = 2;
        SeatHold seatHold = ticketService.findAndHoldSeats(numSeatsToHold, CUSTOMER_EMAIL);
        assertEquals(numSeatsToHold, seatHold.getNumSeatsHeld());
    }

    @Test
    void tryToHoldMoreSeatsAfterNoneAvailable() {
        ticketService.findAndHoldSeats(1_000_000, CUSTOMER_EMAIL);
        SeatHold noneLeft = ticketService.findAndHoldSeats(2, CUSTOMER_EMAIL);
        assertEquals(0, noneLeft.getNumSeatsHeld(), "Should have no seats held");
    }

    @Test
    void seatHoldReducesAvailableSeatCount() {
        int numSeatsToHold = 2;
        SeatHold seatHold = ticketService.findAndHoldSeats(numSeatsToHold, CUSTOMER_EMAIL);
        assertEquals(defaultVenue.getTotalNumSeats() - numSeatsToHold,
                ticketService.numSeatsAvailable(),
                "Seat holds should reduce number of available seats"
        );
    }

    @Test
    void holdMoreSeatsThanAreAvailable() {
        SeatHold seatHold = ticketService.findAndHoldSeats(100_000, CUSTOMER_EMAIL);
        assertEquals(0, ticketService.numSeatsAvailable(), "Should have attempted to hold all remaining seats");
        assertEquals(seatHold.getNumSeatsHeld(),
                defaultVenue.getTotalNumSeats(),
                "Should only have held the total number of seats in existence at the location"
        );
    }

    /**
     * This test version is actually a bit more complicated than the non-parameterized version since it:
     *
     *   1. Introduces a new dependency (junit-jupiter-params)
     *   2. Requires another, persistent TicketService to be set up (to track state across test executions, which could
     *      be troublesome)
     *
     * The test itself is a bit simpler, which is good, but there is lots of additional, slightly hidden complexity,
     * which is bad. I'm not convinced that for this case it is an appropriate usage, however I'll leave this here for
     * demonstration purposes.
     *
     * @param numSeatsRequested number of seats to request for the test execution
     * @param numSeatsExpected
     * @param expectedNumSeatsAvailable
     */
    @DisplayName("Multiple seat holds")
    @ParameterizedTest(name = "[{0}] requested seats should have held [{1}], now [{2}] seats available in venue")
    @CsvSource({ "2, 2, 7", "0, 0, 7", "1, 1, 6", "3, 3, 3", "4, 3, 0", "1, 0, 0", "0, 0, 0" })
    void multipleSeatHoldRequests(int numSeatsRequested, int numSeatsExpected, int expectedNumSeatsAvailable) {
        SeatHold seatHold = persistentTicketService.findAndHoldSeats(numSeatsRequested, "email1");
        assertEquals(expectedNumSeatsAvailable,
                persistentTicketService.numSeatsAvailable(),
                "Seats available does not match expectation"
        );
        assertEquals(seatHold.getNumSeatsHeld(), numSeatsExpected, "Should have held " + numSeatsRequested + " seats");
    }

    @Test
    void reserveSeats() {
        SeatHold seatHold = ticketService.findAndHoldSeats(2, CUSTOMER_EMAIL);
        assertEquals(seatHold.getNumSeatsHeld(), 2, "Should have held 2 seats");
        assertEquals(seatHold.getNumSeatsRequested(), 2, "Should have requested 2 seats");
        String reservationCode = ticketService.reserveSeats(seatHold.getId(), CUSTOMER_EMAIL);
        assertEquals("Reservation code", reservationCode, "Reservation code should match expected");
    }

    @Test
    void ensureSeatHoldsExpire() throws InterruptedException {
        TicketService ticketServiceWithImmediateExpiration = new TicketServiceImpl(defaultVenue, 1L, Duration.ZERO);
        SeatHold seatHold = ticketServiceWithImmediateExpiration.findAndHoldSeats(2, CUSTOMER_EMAIL);
        assertTrue(seatHold.expired(), "SeatHold should expire immediately");
        Thread.sleep(10L); // Ensure that the #expireSeatHolds method has had time to run
        assertEquals(0,
                ((TicketServiceImpl) ticketServiceWithImmediateExpiration).numSeatsHeld(),
                "No seats should be held anymore"
        );
    }

    @Test
    void ensureSeatHoldsDoNotExpireTooSoon() throws InterruptedException {
        final int numSeats = 2;
        TicketService ticketServiceWithSlowExpiration = new TicketServiceImpl(defaultVenue, 1L, Duration.ofDays(1));
        SeatHold seatHold = ticketServiceWithSlowExpiration.findAndHoldSeats(numSeats, CUSTOMER_EMAIL);
        assertFalse(seatHold.expired(), "SeatHold should not be expired yet");
        // TODO: replace this with a spy to ensure that expireSeatHolds has run at least once - timing-dependent tests suck!
        Thread.sleep(10L); // Ensure that the #expireSeatHolds method has had time to run
        assertEquals(numSeats,
                ((TicketServiceImpl) ticketServiceWithSlowExpiration).numSeatsHeld(),
                "Seats should still be held"
        );
    }
}
