package org.dreesbach.ticketing.id;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdGeneratorTest {
    @Test
    public void generatesRandomId() {
        assertThat("Random ID should be > 0", IdGenerator.generateUniqueId(), greaterThan(0));
    }

    @Test
    public void multipleCallsGenerateDifferentIds() {
        int id1 = IdGenerator.generateUniqueId();
        int id2 = IdGenerator.generateUniqueId();
        assertNotEquals(id1, id2, "Random IDs should not be equal");
    }

    @Test
    public void manyCallsShouldExecuteFast() {
        long start = System.currentTimeMillis();
        int numIdsAtStart = IdGenerator.numUniqueIds();
        for (int i = 0; i < 200_000; i++) {
            int id = IdGenerator.generateUniqueId();
        }
        // This is a little bit iffy given different system configurations, but given the implementation it should be very
        // quick to run and hopefully any reasonably recent computer should be able to hit this target. It's important that
        // this can execute quickly to ensure that ID generation does not become a bottleneck in the high-demand environments
        // this will be used in.
        assertThat(
                "Should run in under 1 second - note: may vary per machine! If you have problems with this test sporadically "
                        + "failing, come talk to Carsten and we can find a better solution.",
                System.currentTimeMillis() - start,
                lessThan(1_000L)
        );
        assertEquals(200_000, IdGenerator.numUniqueIds() - numIdsAtStart, "Should have the full number of IDs in use");
    }

    @Test
    public void removeUsedId() {
        int id = IdGenerator.generateUniqueId();
        assertTrue(IdGenerator.retireId(id), "Expected previously-generated ID to be in use");
    }

    @Test
    public void removeUnusedId() {
        assertFalse(IdGenerator.retireId(0), "IDs should start at 1 and 0 should never exist in the list of used IDs");
    }
}
