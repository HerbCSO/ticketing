package org.dreesbach.ticketing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SeatTest {

    private static final String TEST_ID = "TestSeat";
    private Seat seat;

    @BeforeEach
    void setup() {
        this.seat = new SeatImpl(TEST_ID, 0);
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
    void isHeld() {
        assertEquals(false, seat.isHeld(), "Seat should start out as available");
    }

    @Test
    void goodnessForcedToBePositive() {
        TestUtil.testException(
                IllegalArgumentException.class,
                () -> new SeatImpl("test", -1),
                "goodness should be a positive value"
        );
    }

    @Test
    void idNotNull() {
        TestUtil.testException(
                NullPointerException.class,
                () -> new SeatImpl(null, 1),
                "id should not be null"
        );
    }

    @Test
    void hold() {
        seat.hold();
        assertAll("check conditions",
                () -> assertEquals(false, seat.isAvailable(), "Holding a seat should make it unavailable"),
                () -> assertEquals(false, seat.isReserved(), "Seat should not be reserved")
        );
    }

    @Test
    void cancelHold() {
        seat.hold();
        assertAll("ensure hold",
                () -> assertEquals(false, seat.isAvailable(), "Make sure seat got held"),
                () -> assertEquals(false, seat.isReserved(), "Seat should not be reserved")
        );
        seat.cancelHold();
        assertAll("check cancellation",
                () -> assertEquals(true, seat.isAvailable(), "Releasing an unreserved seat should work"),
                () -> assertEquals(false, seat.isReserved(), "Seat should not be reserved")
        );
    }

    @Test
    void cancelHoldOnUnheldSeat() {
        TestUtil.testException(
                IllegalStateException.class,
                () -> seat.cancelHold(),
                "Seat was already available"
        );
    }

    @Test
    void holdAlreadyHeldSeat() {
        seat.hold();
        TestUtil.testException(
                IllegalStateException.class,
                () -> seat.hold(),
                "Cannot hold an unavailable seat"
        );
    }

    @Test
    void releaseHeldSeat() {
        seat.hold();
        assertEquals(false, seat.isAvailable(), "Make sure seat got held");
        seat.cancelHold();
        assertAll("check conditions",
                () -> assertEquals(true, seat.isAvailable(), "A released seat should be available for reservation again"),
                () -> assertEquals(false, seat.isReserved(), "Seat should not be reserved")
        );
    }

    @Test
    void reserve() {
        seat.hold(); // have to hold before reserving
        seat.reserve();
        assertAll("check conditions",
                () -> assertEquals(false, seat.isAvailable(), "Reserving a seat should also make it unavailable"),
                () -> assertEquals(true, seat.isReserved(), "Reserving a seat should mark it as such")
        );
    }

    @Test
    void reserveUnheldSeat() {
        TestUtil.testException(
                IllegalStateException.class,
                () -> seat.reserve(),
                "Seat was still marked as available"
        );
    }

    @Test
    void releaseReservedSeat() {
        seat.hold(); // have to hold before reserving
        seat.reserve();
        assertAll("before cancellation",
                () -> assertEquals(false, seat.isAvailable(), "Make sure seat got held"),
                () -> assertEquals(true, seat.isReserved(), "Make sure seat got reserved")
        );
        seat.cancelReservation();
        assertAll("after cancellation",
                () -> assertEquals(true, seat.isAvailable(), "A released seat should be available for reservation again"),
                () -> assertEquals(false, seat.isReserved(), "Seat should not be reserved")
        );
    }

    @Test
    void cancelReservationOnUnreservedSeat() {
        TestUtil.testException(
                IllegalStateException.class,
                () -> seat.cancelReservation(),
                "Seat was still marked as available"
        );
    }

    @Test
    void reserveAlreadyReservedSeat() {
        seat.hold();
        seat.reserve();
        TestUtil.testException(
                IllegalStateException.class,
                () -> seat.reserve(),
                "Seat was already reserved"
        );
    }

    @Test
    void releaseUnHeldSeat() {
        TestUtil.testException(
                IllegalStateException.class,
                () -> seat.cancelHold(),
                "Seat was already available"
        );
    }

    @Test
    void releaseUnHeldReservedSeat() {
        seat.hold();
        seat.reserve();
        TestUtil.testException(
                IllegalStateException.class,
                () -> seat.cancelHold(),
                "Cannot cancel a hold on an already-reserved seat"
        );
    }

    @Test
    void releaseUnReservedSeat() {
        TestUtil.testException(
                IllegalStateException.class,
                () -> seat.cancelReservation(),
                "Seat was still marked as available"
        );
    }

    @Test
    void releaseUnReservedButHeldSeat() {
        seat.hold();
        TestUtil.testException(
                IllegalStateException.class,
                () -> seat.cancelReservation(),
                "Seat was not reserved"
        );
    }
}
