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
     * Whether this seat is available for holding or not.
     *
     * @return true if it is available
     */
    boolean isAvailable();

    /**
     * Whether this seat is reserved or not.
     *
     * @return true if it is reserved
     */
    boolean isReserved();

    /**
     * Hold this seat.
     */
    void hold();

    /**
     * See if the seat is currently held or not. Convenience method, this simply returns {@code !isAvailable()}.
     *
     * @return true if it is held
     */
    boolean isHeld();

    /**
     * Release this seat from a previous hold.
     */
    void cancelHold();

    /**
     * Actually reserve this seat.
     */
    void reserve();

    /**
     * Cancel a previous reservation, making the seat available again.
     */
    void cancelReservation();

    /**
     * Return a measure of the seat's "goodness", i.e. how desirable the seat is.
     *
     * @return an int that measures how good the seat is - higher number = better seat
     */
    double seatGoodness();
}
