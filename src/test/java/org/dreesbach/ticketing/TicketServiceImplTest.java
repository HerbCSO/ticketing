package org.dreesbach.ticketing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketServiceImplTest {
    private static final String CUSTOMER_EMAIL = "me@you.com";
    private SeatingArrangement defaultSeatingArrangement;
    private TicketService ticketService;

    @BeforeEach
    public void setup() {
        SeatPickingStrategy<SimpleSeatingArrangement> seatPickingStrategy =
                new SimpleSeatingArrangementSimpleSeatPickingStrategy();
        this.defaultSeatingArrangement = new SimpleSeatingArrangement(3, 3, seatPickingStrategy);
        this.ticketService = new TicketServiceImpl(defaultSeatingArrangement);
    }

    @Test
    void allSeatsAvailable() {
        assertEquals(
                defaultSeatingArrangement.getTotalNumSeats(),
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
        assertEquals(null, noneLeft);
    }

    @Test
    void seatHoldReducesAvailableSeatCount() {
        int numSeatsToHold = 2;
        SeatHold seatHold = ticketService.findAndHoldSeats(numSeatsToHold, CUSTOMER_EMAIL);
        assertEquals(defaultSeatingArrangement.getTotalNumSeats() - numSeatsToHold,
                ticketService.numSeatsAvailable(),
                "Seat holds should reduce number of available seats"
        );
    }

    @Test
    void holdMoreSeatsThanAreAvailable() {
        SeatHold seatHold = ticketService.findAndHoldSeats(100_000, CUSTOMER_EMAIL);
        assertEquals(0, ticketService.numSeatsAvailable(), "Should have attempted to hold all remaining seats");
        assertEquals(seatHold.getNumSeatsHeld(),
                defaultSeatingArrangement.getTotalNumSeats(),
                "Should only have reserved the total number of seats in existence at the location"
        );
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
        TicketService ticketServiceWithImmediateExpiration =
                new TicketServiceImpl(defaultSeatingArrangement, 1L, Duration.ZERO);
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
        TicketService ticketServiceWithSlowExpiration =
                new TicketServiceImpl(defaultSeatingArrangement, 1L, Duration.ofDays(1));
        SeatHold seatHold = ticketServiceWithSlowExpiration.findAndHoldSeats(numSeats, CUSTOMER_EMAIL);
        assertFalse(seatHold.expired(), "SeatHold should not be expired yet");
        // TODO: replace this with a spy to ensure that expireSeatHolds has run at least once - timing-dependent tests suck!
        Thread.sleep(10L); // Ensure that the #expireSeatHolds method has had time to run
        assertEquals(
                numSeats,
                ((TicketServiceImpl) ticketServiceWithSlowExpiration).numSeatsHeld(),
                "Seats should still be held"
        );
    }
}
