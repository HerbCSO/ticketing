package org.dreesbach.ticketing;

import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestUtil {
    static void testException(
            final Class<? extends Throwable> expectedType, final Executable test, final String expectedMessage
    ) {
        Throwable exception = assertThrows(expectedType, test, "Exception expected");
        assertEquals(expectedMessage, exception.getMessage(), "Different exception message than expected");
    }
}
