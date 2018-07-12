package org.dreesbach.ticketing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SeatTest {

    private static final String TEST_ID = "TestSeat";
    private Seat seat;

    @BeforeEach
    void setup() {
        this.seat = new SeatImpl(TEST_ID);
    }

    @Test
    void getId() {
        assertEquals(TEST_ID, seat.getId(), "Expect seat ID not to change");
    }

    @Test
    void isAvailable() {
        assertEquals(true, seat.isAvailable(), "Seat should start out as available");
    }

    @Test
    void hold() {
        seat.hold();
        assertEquals(false, seat.isAvailable(), "Holding a seat should make it unavailable");
        assertEquals(false, seat.isReserved(), "Seat should not be reserved");
    }

    @Test
    void cancelHold() {
        seat.hold();
        assertEquals(false, seat.isAvailable(), "Make sure seat got held");
        assertEquals(false, seat.isReserved(), "Seat should not be reserved");
        seat.cancelHold();
        assertEquals(true, seat.isAvailable(), "Releasing an unreserved seat should work");
        assertEquals(false, seat.isReserved(), "Seat should not be reserved");
    }

    @Test
    void cancelHoldOnUnheldSeat() {
        Throwable exception = assertThrows(IllegalStateException.class, () -> seat.cancelHold());
        assertEquals("Seat was already available", exception.getMessage(), "Different exception message than expected");
    }

    @Test
    void holdAlreadyHeldSeat() {
        seat.hold();
        Throwable exception = assertThrows(IllegalStateException.class, () -> seat.hold());
        assertEquals("Cannot hold an unavailable seat", exception.getMessage(), "Different exception message than expected");
    }

    @Test
    void releaseHeldSeat() {
        seat.hold();
        assertEquals(false, seat.isAvailable(), "Make sure seat got held");
        seat.cancelHold();
        assertEquals(true, seat.isAvailable(), "A released seat should be available for reservation again");
        assertEquals(false, seat.isReserved(), "Seat should not be reserved");
    }

    @Test
    void reserve() {
        seat.hold(); // have to hold before reserving
        seat.reserve();
        assertEquals(false, seat.isAvailable(), "Reserving a seat should also make it unavailable");
        assertEquals(true, seat.isReserved(), "Reserving a seat should mark it as such");
    }

    @Test
    void reserveUnheldSeat() {
        Throwable exception = assertThrows(IllegalStateException.class, () -> seat.reserve());
        assertEquals("Seat was still marked as available", exception.getMessage(), "Different exception message than expected");
    }

    @Test
    void releaseReservedSeat() {
        seat.hold(); // have to hold before reserving
        seat.reserve();
        assertEquals(false, seat.isAvailable(), "Make sure seat got held");
        assertEquals(true, seat.isReserved(), "Make sure seat got reserved");
        seat.cancelReservation();
        assertEquals(true, seat.isAvailable(), "A released seat should be available for reservation again");
        assertEquals(false, seat.isReserved(), "Seat should not be reserved");
    }

    @Test
    void cancelReservationOnUnreservedSeat() {
        Throwable exception = assertThrows(IllegalStateException.class, () -> seat.cancelReservation());
        assertEquals("Seat was still marked as available", exception.getMessage(), "Different exception message than expected");
    }

    @Test
    void reserveAlreadyReservedSeat() {
        seat.hold();
        seat.reserve();
        Throwable exception = assertThrows(IllegalStateException.class, () -> seat.reserve());
        assertEquals("Seat was already reserved", exception.getMessage(), "Different exception message than expected");
    }

    @Test
    void releaseUnHeldSeat() {
        Throwable exception = assertThrows(IllegalStateException.class, () -> seat.cancelHold());
        assertEquals("Seat was already available", exception.getMessage(), "Different exception message than expected");
    }

    @Test
    void releaseUnHeldReservedSeat() {
        seat.hold();
        seat.reserve();
        Throwable exception = assertThrows(IllegalStateException.class, () -> seat.cancelHold());
        assertEquals(
                "Cannot cancel a hold on an already-reserved seat",
                exception.getMessage(),
                "Different exception message than expected"
        );
    }

    @Test
    void releaseUnReservedSeat() {
        Throwable exception = assertThrows(IllegalStateException.class, () -> seat.cancelReservation());
        assertEquals("Seat was still marked as available", exception.getMessage(), "Different exception message than expected");
    }

    @Test
    void releaseUnReservedButHeldSeat() {
        seat.hold();
        Throwable exception = assertThrows(IllegalStateException.class, () -> seat.cancelReservation());
        assertEquals("Seat was not reserved", exception.getMessage(), "Different exception message than expected");
    }
}
