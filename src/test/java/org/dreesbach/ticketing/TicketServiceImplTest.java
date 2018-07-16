package org.dreesbach.ticketing;

import org.dreesbach.ticketing.id.IdGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketServiceImplTest {
    private static final String CUSTOMER_EMAIL = "me@you.com";
    private static final int NUM_ROWS = 3;
    private static final int NUM_COLS = 3;
    private static final long SEAT_HOLD_EXPIRATION_RUN_WAIT_TIME_IN_MILLIS = 100L;
    private static final Duration DEFAULT_SEAT_HOLD_CHECK_DURATION = Duration.ofMillis(1);
    private static Venue persistentDefaultVenue;
    private static TicketService persistentTicketService;
    private Venue defaultVenue;
    private TicketService ticketService;

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
    void tooShortEmail() {
        Throwable exception = assertThrows(IllegalArgumentException.class,
                () -> ticketService.findAndHoldSeats(2, "a"),
                "Expected exception"
        );
        assertEquals("C'mon, you think [a] is an email address!? ;]", exception.getMessage(), "Wrong exception message");
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
        assertAll("check postconditions",
                () -> assertEquals(0, ticketService.numSeatsAvailable(), "Should have attempted to hold all remaining seats"),
                () -> assertEquals(seatHold.getNumSeatsHeld(),
                        defaultVenue.getTotalNumSeats(),
                        "Should only have held the total number of seats in existence at the location"
                )
        );
    }

    @Test
    void holdZeroSeatsThrowsException() {
        Throwable exception = assertThrows(IllegalArgumentException.class,
                () -> ticketService.findAndHoldSeats(0, CUSTOMER_EMAIL),
                "Exception expected"
        );
        assertEquals("numSeatsToHold must be > 0", exception.getMessage(), "Wrong exception message");
    }

    @Test
    void negativeSeatHoldExpirationTimeThrowsException() {
        Throwable exception = assertThrows(IllegalArgumentException.class,
                () -> new TicketServiceImpl(defaultVenue, Duration.ofSeconds(-1), Duration.ZERO),
                "Exception expected"
        );
        assertEquals("seatHoldCheckExpiration must be > 0", exception.getMessage(), "Wrong exception message");
    }

    /**
     * This test version is actually a bit more complicated than the non-parameterized version since it:
     * <ol>
     * <li>Introduces a new dependency (junit-jupiter-params)</li>
     * <li>Requires another, persistent TicketService to be set up (to track state across test executions, which could be
     * troublesome)</li>
     * </ol>
     * The test itself is a bit simpler, which is good, but there is lots of additional, slightly hidden complexity, which is
     * bad. I'm not convinced that for this case it is an appropriate usage, however I'll leave this here for demonstration
     * purposes.
     *
     * @param numSeatsRequested number of seats to request for the test execution
     * @param numSeatsExpected
     * @param expectedNumSeatsAvailable
     */
    @DisplayName("Multiple seat holds")
    @ParameterizedTest(name = "[{0}] requested seats should have held [{1}], now [{2}] seats available in venue")
    @CsvSource({ "2, 2, 7", "1, 1, 6", "3, 3, 3", "4, 3, 0", "1, 0, 0" })
    void multipleSeatHoldRequests(int numSeatsRequested, int numSeatsExpected, int expectedNumSeatsAvailable) {
        SeatHold seatHold = persistentTicketService.findAndHoldSeats(numSeatsRequested, "email" + numSeatsRequested);
        persistentDefaultVenue.printSeats();
        assertEquals(persistentTicketService.numSeatsAvailable(),
                expectedNumSeatsAvailable,
                "Seats available does not match expectation"
        );
        assertEquals(numSeatsExpected, seatHold.getNumSeatsHeld(), "Should have held " + numSeatsExpected + " seats");
    }

    @Test
    void reserveSeats() {
        SeatHold seatHold = ticketService.findAndHoldSeats(2, CUSTOMER_EMAIL);
        assertEquals(seatHold.getNumSeatsHeld(), 2, "Should have held 2 seats");
        assertEquals(seatHold.getNumSeatsRequested(), 2, "Should have requested 2 seats");
        String reservationCode = ticketService.reserveSeats(seatHold.getId(), CUSTOMER_EMAIL);
        assertThat("Reservation code should match expected", reservationCode, matchesPattern("[A-Z0-9]{6}"));
        defaultVenue.printSeats();
    }

    @Test
    void reserveUnheldSeat() {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> ticketService.reserveSeats(0, "test"));
        assertEquals("seatHoldId must be > 0", exception.getMessage(), "Exception message didn't match expectation");
        int id = IdGenerator.generateUniqueIntId();
        exception = assertThrows(IllegalStateException.class, () -> ticketService.reserveSeats(id, "test"));
        assertEquals("SeatHold ID [" + id + "] not found",
                exception.getMessage(),
                "Exception message didn't match " + "expectation"
        );
        IdGenerator.retireId(id);
    }

    @Test
    void ensureSeatHoldsExpire() throws InterruptedException {
        TicketService ticketServiceWithImmediateExpiration =
                new TicketServiceImpl(defaultVenue, DEFAULT_SEAT_HOLD_CHECK_DURATION, Duration.ZERO);
        SeatHold seatHold = ticketServiceWithImmediateExpiration.findAndHoldSeats(2, CUSTOMER_EMAIL);
        assertTrue(seatHold.expired(), "SeatHold should expire immediately");
        Thread.sleep(SEAT_HOLD_EXPIRATION_RUN_WAIT_TIME_IN_MILLIS); // Ensure that the #expireSeatHolds method has had time
        // to run
        assertEquals(0,
                ((TicketServiceImpl) ticketServiceWithImmediateExpiration).numSeatsHeld(),
                "No seats should be held anymore"
        );
        defaultVenue.printSeats();
    }

    @Test
    void reserveExpiredSeats() throws InterruptedException {
        TicketService ticketServiceWithImmediateExpiration =
                new TicketServiceImpl(defaultVenue, DEFAULT_SEAT_HOLD_CHECK_DURATION, Duration.ZERO);
        SeatHold seatHold = ticketServiceWithImmediateExpiration.findAndHoldSeats(2, CUSTOMER_EMAIL);
        assertTrue(seatHold.expired(), "SeatHold should expire immediately");
        Thread.sleep(SEAT_HOLD_EXPIRATION_RUN_WAIT_TIME_IN_MILLIS); // Ensure that the #expireSeatHolds method has had time
        // to run
        Throwable exception = assertThrows(IllegalStateException.class,
                () -> ticketServiceWithImmediateExpiration.reserveSeats(seatHold.getId(), CUSTOMER_EMAIL),
                "Expected exception"
        );
        assertEquals("SeatHold ID [" + seatHold.getId() + "] not found", exception.getMessage(), "Wrong exception message");
    }

    @Test
    void ensureSeatHoldsDoNotExpireTooSoon() throws InterruptedException {
        final int numSeats = 2;
        TicketServiceImpl ticketServiceWithSlowExpiration =
                new TicketServiceImpl(defaultVenue, DEFAULT_SEAT_HOLD_CHECK_DURATION, Duration.ofDays(1));
        SeatHold seatHold = ticketServiceWithSlowExpiration.findAndHoldSeats(numSeats, CUSTOMER_EMAIL);
        assertFalse(seatHold.expired(), "SeatHold should not be expired yet");
        // Bugger - a Mockito spy seems to be unable to catch the fact that the expiration method was called from a separate
        // thread (the ScheduledExecutorService), so I admit defeat for now and will keep the sleep in here. :/
        Thread.sleep(SEAT_HOLD_EXPIRATION_RUN_WAIT_TIME_IN_MILLIS); // Ensure that the #expireSeatHolds method has had time
        // to run
        assertEquals(numSeats, ticketServiceWithSlowExpiration.numSeatsHeld(), "Seats should still be held");
        defaultVenue.printSeats();
    }

    @Test
    void stadiumSizedVenue() {
        SeatPickingStrategy<RectangularVenue> seatPickingStrategy = new RectangularVenueSimpleSeatPickingStrategy();
        Venue venue = new RectangularVenue(100, 1_000, seatPickingStrategy);
        TicketService localTicketService = new TicketServiceImpl(venue, Duration.ofMillis(500), Duration.ofSeconds(2));
        SecureRandom rnd = new SecureRandom();
        rnd.setSeed(Instant.now().getEpochSecond());
        Map<String, SeatHold> seatHolds = new HashMap<>();
        int counter = 0;
        while (venue.getAvailableNumSeats() > 0) {
            String email = "email" + counter;
            seatHolds.put(email, localTicketService.findAndHoldSeats(rnd.nextInt(20) + 1, email));
            counter++;
            if (counter % 1000 == 0) {
                System.out.println(counter + " SeatHolds created");
                System.out.println(venue.getAvailableNumSeats() + " seats available out of " + venue.getTotalNumSeats());
            }
        }
        counter = 0;
        for (Map.Entry<String, SeatHold> seatHold : seatHolds.entrySet()) {
            try {
                localTicketService.reserveSeats(seatHold.getValue().getId(), seatHold.getKey());
            }
            catch (IllegalStateException ise) {
                counter--;
            }
            counter++;
            if (counter % 1000 == 0) {
                System.out.println(counter + " SeatHolds reserved");
            }
        }
        assertAll("check postconditions",
                () -> assertEquals(0, venue.getAvailableNumSeats(), "Expcted 0 seats left"),
                () -> assertEquals(0, localTicketService.numSeatsAvailable(), "Expected 0 seats available"),
                () -> assertThat("Expected more than 1,000 seat holds to still not be expired",
                        ((TicketServiceImpl) localTicketService).numSeatsHeld(),
                        greaterThanOrEqualTo(1_000)
                )
        );
    }
}
