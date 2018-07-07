package org.dreesbach.ticketing;

public class TicketServiceImpl implements TicketService {
    private SeatingArrangement seatingArrangement;

    public TicketServiceImpl(SeatingArrangement seatingArrangement) {
        this.seatingArrangement = seatingArrangement;
    }

    @Override
    public int numSeatsAvailable() {
        return seatingArrangement.getNumSeats();
    }

    @Override
    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        return new SeatHold();
    }

    @Override
    public String reserveSeats(int seatHoldId, String customerEmail) {
        return null;
    }
}
