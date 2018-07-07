package org.dreesbach.ticketing.id;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.jupiter.api.Assertions.*;

class IdGeneratorTest {
    private static String EMAIL1 = "me@you.com";
    private static String EMAIL2 = "you@me.com";

    @Test
    public void generatesRandomId() {
        assertThat("Random ID should be > 0", IdGenerator.generateUniqueId(EMAIL1), greaterThan(0));
    }

    @Test
    public void multipleCallsWithSameEmailGenerateDifferentIds() {
        int id1 = IdGenerator.generateUniqueId(EMAIL1);
        int id2 = IdGenerator.generateUniqueId(EMAIL1);
        assertNotEquals(id1, id2, "Random IDs should not be equal");
    }

    @Test
    public void callsWithDifferentEmailsGenerateDifferentIds() {
        int id1 = IdGenerator.generateUniqueId(EMAIL1);
        int id2 = IdGenerator.generateUniqueId(EMAIL2);
        assertNotEquals(id1, id2, "Random IDs should not be equal");
    }
}
