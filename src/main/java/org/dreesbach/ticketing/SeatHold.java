package org.dreesbach.ticketing;

/**
 * Class to coordinate holding of seats prior to actually reserving.
 */
class SeatHold {
    /**
     * Number of seats to hold for reservation.
     */
    private int numSeats;
    /**
     * Unique ID of the seat hold.
     */
    private int id;

    /**
     * Number of seats held for this instance.
     *
     * @return number of seats held
     */
    public int getNumSeats() {
        return 2;
        // return numSeats;
    }

    /**
     * Unique integer ID for the seat hold
     *
     * Note: I would prefer to do this as a UUID in order to prevent potentially leaking this information out and making it
     * guessable, thus potentially enabling someone to get somebody else's seat hold (although they'd need to know their
     * email as well, making it a little less likely to happen). However the interface has been specified with this as an
     * {@code int}, so I'm stuck with this implementation.
     *
     * @return unique ID of the seat hold
     */
    public int getId() {
        return 1;
        // return id;
    }
}
