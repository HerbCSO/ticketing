package org.dreesbach.ticketing;

import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
    void reserve() {
        seat.reserve();
        assertEquals(false, seat.isAvailable(), "Reserving a seat should make it unavailable");
    }

    @Test
    void release() {
        seat.release();
        assertEquals(true, seat.isAvailable(), "Releasing an unreserved seat should work");
    }

    @Test
    void releaseReservedSeat() {
        seat.reserve();
        assertEquals(false, seat.isAvailable(), "Make sure seat got reserved");
        seat.release();
        assertEquals(true, seat.isAvailable(), "A released seat should be available for reservation again");
    }
}
