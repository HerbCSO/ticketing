package org.dreesbach.ticketing;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

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
     * Indicates how good the seat is.
     */
    private double goodness;

    /**
     * Default constructor.
     *
     * @param id the {@link String} identifier for the seat
     * @param goodness how good the seat is - lower number = better seat
     */
    SeatImpl(final String id, final double goodness) {
        checkArgument(goodness >= 0.0, "goodness should be a positive value");
        this.id = checkNotNull(id, "id should not be null");
        available = true;
        reserved = false;
        this.goodness = goodness;
    }

    @Override
    public void hold() {
        checkState(available, "Cannot hold an unavailable seat");
        checkState(!reserved, "Seat was already reserved");
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
        checkState(!available, "Seat was already available");
        checkState(!reserved, "Cannot cancel a hold on an already-reserved seat");
        available = true;
        reserved = false;
    }

    @Override
    public void reserve() {
        checkState(!available, "Seat was still marked as available");
        checkState(!reserved, "Seat was already reserved");
        available = false;
        reserved = true;
    }

    @Override
    public void cancelReservation() {
        checkState(!available, "Seat was still marked as available");
        checkState(reserved, "Seat was not reserved");
        available = true;
        reserved = false;
    }

    @Override
    public double seatGoodness() {
        return goodness;
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
