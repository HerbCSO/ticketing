package org.dreesbach.ticketing;

/**
 * An object that represents an individual seat in the location.
 */
public interface Seat {
    /**
     * The ID of the seat, e.g. A1, R32, or whatever numbering system the location uses.
     *
     * @return seat ID
     */
    String getId();

    /**
     * Whether this seat is available or not.
     *
     * @return true if it available
     */
    boolean isAvailable();

    /**
     * Reserve this seat.
     */
    void reserve();

    /**
     * Release this seat from a previous reservation.
     */
    void release();
}
