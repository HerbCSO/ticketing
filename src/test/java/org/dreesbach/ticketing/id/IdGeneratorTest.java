package org.dreesbach.ticketing.id;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdGeneratorTest {

    private static final int NUM_ID_GENERATION_ITERATIONS = 200_000;
    private static final long MAX_ID_GENERATION_RUNTIME_IN_MS = 2_000L;

    @Test
    public void generatesRandomId() {
        assertThat("Random ID should be > 0", IdGenerator.generateUniqueIntId(), greaterThan(0));
    }

    @Test
    public void multipleCallsGenerateDifferentIds() {
        int id1 = IdGenerator.generateUniqueIntId();
        int id2 = IdGenerator.generateUniqueIntId();
        assertNotEquals(id1, id2, "Random IDs should not be equal");
    }

    @Test
    public void manyCallsShouldExecuteFast() {
        long start = System.currentTimeMillis();
        int numIdsAtStart = IdGenerator.numUniqueIds();
        for (int i = 0; i < NUM_ID_GENERATION_ITERATIONS; i++) {
            IdGenerator.generateUniqueIntId();
        }
        assertAll("check postconditions",
                // This is a little bit iffy given different system configurations, but given the implementation it should be
                // very quick to run and hopefully any reasonably recent computer should be able to hit this target. It's
                // important that this can execute quickly to ensure that ID generation does not become a bottleneck in the
                // high-demand environments this will be used in.
                () -> assertThat("Should run in under " + MAX_ID_GENERATION_RUNTIME_IN_MS
                                + " ms - note: may vary per machine! If you have problems with this test sporadically "
                                + "failing, come " + "talk to Carsten and we can find a better solution.",
                        System.currentTimeMillis() - start,
                        lessThan(MAX_ID_GENERATION_RUNTIME_IN_MS)
                ), () -> assertEquals(NUM_ID_GENERATION_ITERATIONS,
                        IdGenerator.numUniqueIds() - numIdsAtStart,
                        "Should have the full number of IDs in use"
                )
        );
    }

    @Test
    public void manyReservationIdCallsShouldExecuteFast() {
        long start = System.currentTimeMillis();
        int numIdsAtStart = IdGenerator.numUniqueReservationIds();
        for (int i = 0; i < NUM_ID_GENERATION_ITERATIONS; i++) {
            IdGenerator.generateReservationCode();
        }
        assertAll("check postconditions",
                // See the comment in manyCallsShouldExecuteFast
                () -> assertThat("Should run in under " + MAX_ID_GENERATION_RUNTIME_IN_MS
                                + " ms - note: may vary per machine! If you have problems with this test sporadically "
                                + "failing, come "
                                + "talk to Carsten and we can find a better solution.",
                        System.currentTimeMillis() - start,
                        lessThan(MAX_ID_GENERATION_RUNTIME_IN_MS)
                ), () -> assertEquals(NUM_ID_GENERATION_ITERATIONS,
                        IdGenerator.numUniqueReservationIds() - numIdsAtStart,
                        "Should have the full number of IDs in use"
                )
        );
    }

    @Test
    public void removeUsedId() {
        int id = IdGenerator.generateUniqueIntId();
        assertTrue(IdGenerator.retireId(id), "Expected previously-generated ID to be in use");
    }

    @Test
    public void removeUnusedId() {
        assertFalse(IdGenerator.retireId(0), "IDs should start at 1 and 0 should never exist in the list of used IDs");
    }

    @Test
    public void removeUsedReservationId() {
        String id = IdGenerator.generateReservationCode();
        assertTrue(IdGenerator.retireReservationId(id), "Expected previously-generated ID to be in use");
    }

    @Test
    public void removeUnusedReservationId() {
        assertFalse(
                IdGenerator.retireReservationId("------"),
                "IDs only have uppercase alphanumeric characters and this should never exist in the list of used IDs"
        );
    }

    @Test
    public void testUniqueIntIdsGenerated() {
        Set<Integer> ids = new HashSet<>();
        for (int i = 0; i < NUM_ID_GENERATION_ITERATIONS; i++) {
            assertTrue(ids.add(IdGenerator.generateUniqueIntId()), "Only unique IDs expected");
        }
    }

    @Test
    public void testUniqueReservationIdsGenerated() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < NUM_ID_GENERATION_ITERATIONS; i++) {
            assertTrue(ids.add(IdGenerator.generateReservationCode()), "Only unique IDs expected");
        }
    }
}
