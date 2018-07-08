package org.dreesbach.ticketing;

/**
 * The default impelementation of the {@link Seat} interface.
 */
public final class SeatImpl implements Seat {
    /**
     * Indicates if tis seat is available for reservation.
     */
    private boolean available;
    /**
     * The identifier for the seat, e.g. K15, 203, 1A, whatever.
     *
     * Should be unique for each seat in the locale in order to enable identification of seats, however this is not enforced in
     * this implementation, it is left up to the {@link SeatingArrangement} to manage that.
     */
    private final String id;

    /**
     * Default constructor.
     *
     * @param id the {@link String} identifier for the seat
     */
    SeatImpl(final String id) {
        this.id = id;
        available = true;
    }

    @Override
    public void reserve() {
        available = false;
    }

    @Override
    public void release() {
        available = true;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }
}
