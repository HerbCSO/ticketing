package org.dreesbach.ticketing;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketServiceImplTest {
    private SeatingArrangement defaultSeatingArrangement;
    private TicketService ticketService;

    @BeforeEach
    public void setup() {
        this.defaultSeatingArrangement = new SimpleSeatingArrangement(3,3);
        this.ticketService = new TicketServiceImpl(defaultSeatingArrangement);
    }

    @Test
    void allSeatsAvailable() {
        assertEquals(defaultSeatingArrangement.getTotalNumSeats(), ticketService.numSeatsAvailable(), "Should have all seats "
                + "available");
    }

    @Test
    void findAndHoldSeats() {
        int numSeatsToHold = 2;
        SeatHold seatHold = ticketService.findAndHoldSeats(numSeatsToHold, "me@you.com");
        assertEquals(numSeatsToHold, seatHold.getNumSeats());
    }

    @Test
    void seatHoldReducesAvailableSeatCount() {
        int numSeatsToHold = 2;
        SeatHold seatHold = ticketService.findAndHoldSeats(numSeatsToHold, "me@you.com");
        assertEquals(defaultSeatingArrangement.getTotalNumSeats() - numSeatsToHold, ticketService.numSeatsAvailable(),
                "Seat holds should reduce number of available seats");
    }

    @Test
    void reserveMoreSeatsThanAreAvailable() {
        SeatHold seatHold = ticketService.findAndHoldSeats(100_000, "me@you.com");
        assertEquals(0, ticketService.numSeatsAvailable(), "Should have attempted to reserve all remaining seats");
        assertEquals(seatHold.getNumSeats(), defaultSeatingArrangement.getTotalNumSeats(),
                "Should only have reserved the total number of seats in existence at the location");
    }

    @Test
    void reserveSeats() {
        String customerEmail = "me@you.com";
        SeatHold seatHold = ticketService.findAndHoldSeats(2, customerEmail);
        String reservationCode = ticketService.reserveSeats(seatHold.getId(), customerEmail);
        assertEquals("Reservation code", reservationCode, "Reservation code should match expected");
    }
}
