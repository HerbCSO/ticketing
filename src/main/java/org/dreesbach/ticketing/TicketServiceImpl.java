package org.dreesbach.ticketing;

/**
 * Default implementation of TicketService interface.
 */
final class TicketServiceImpl implements TicketService {
    /**
     * A seating arrangement passed into this class.
     *
     * Can be a simple arectangular arrangement, or more complex.
     */
    private SeatingArrangement seatingArrangement;

    /**
     * Default constructor.
     *
     * @param seatingArrangement an implementation of {@see SeatingArrangement}
     */
    TicketServiceImpl(final SeatingArrangement seatingArrangement) {
        this.seatingArrangement = seatingArrangement;
    }

    @Override
    public int numSeatsAvailable() {
        return seatingArrangement.getNumSeats();
    }

    @Override
    public SeatHold findAndHoldSeats(final int numSeats, final String customerEmail) {
        return new SeatHold();
    }

    @Override
    public String reserveSeats(final int seatHoldId, final String customerEmail) {
        return null;
    }
}
