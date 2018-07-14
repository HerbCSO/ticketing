package org.dreesbach.ticketing;

/**
 * The default impelementation of the {@link Seat} interface.
 *
 * Implementation note: the available/reserved states are managed as two booleans right now, but if more states should be
 * added, this should then be implemented as a state machine. It's manageable for two states, but any more complicated than
 * that and it should be changed.
 */
public final class SeatImpl implements Seat {
    /**
     * The identifier for the seat, e.g. K15, 203, 1A, whatever.
     * <p>
     * Should be unique for each seat in the locale in order to enable identification of seats, however this is not enforced in
     * this implementation, it is left up to the {@link Venue} to manage that.
     */
    private final String id;
    /**
     * Indicates if tis seat is available for a hold.
     */
    private boolean available;
    /**
     * Indicates if this seat is reserved.
     */
    private boolean reserved;

    /**
     * Default constructor.
     *
     * @param id the {@link String} identifier for the seat
     */
    SeatImpl(final String id) {
        this.id = id;
        available = true;
        reserved = false;
    }

    @Override
    public void hold() {
        if (!available) {
            throw new IllegalStateException("Cannot hold an unavailable seat");
        }
        if (reserved) {
            throw new IllegalStateException("Seat was already reserved");
        }
        available = false;
        reserved = false;
    }

    @Override
    public boolean isHeld() {
        return !isAvailable();
    }

    @Override
    public void cancelHold() {
        // If the seat was already available before this call, we want to throw an exception because we want to know whether
        // we're releasing seats multiple times.
        if (available) {
            throw new IllegalStateException("Seat was already available");
        }
        if (reserved) {
            throw new IllegalStateException("Cannot cancel a hold on an already-reserved seat");
        }
        available = true;
        reserved = false;
    }

    @Override
    public void reserve() {
        if (available) {
            throw new IllegalStateException("Seat was still marked as available");
        }
        if (reserved) {
            throw new IllegalStateException("Seat was already reserved");
        }
        available = false;
        reserved = true;
    }

    @Override
    public void cancelReservation() {
        if (available) {
            throw new IllegalStateException("Seat was still marked as available");
        }
        if (!reserved) {
            throw new IllegalStateException("Seat was not reserved");
        }
        available = true;
        reserved = false;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public boolean isReserved() {
        return reserved;
    }
}
