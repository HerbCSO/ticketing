package org.dreesbach.ticketing;

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
        assertEquals(defaultSeatingArrangement.getNumSeats(), ticketService.numSeatsAvailable(), "Should have all seats "
                + "available");
    }

    @Test
    void findAndHoldSeats() {
        SeatHold seatHold = ticketService.findAndHoldSeats(2, "me@you.com");
        assertEquals(2, seatHold.getNumSeats());
    }

    @Test
    void reserveSeats() {
        String customerEmail = "me@you.com";
        SeatHold seatHold = ticketService.findAndHoldSeats(2, customerEmail);
        ticketService.reserveSeats(seatHold.getId(), customerEmail);
    }
}
