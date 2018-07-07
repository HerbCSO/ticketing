package org.dreesbach.ticketing.id;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Implementation of the IdGenerator interface.
 */
public final class IdGenerator {
    /**
     * Make utility class non-instantiable.
     */
    private IdGenerator() { }

    /**
     * Tune this to match the expected workloads. The more seat reservation requests that are expected, the more you may want
     * to increase this value.
     */
    private static final int INITIAL_CAPACITY = 100_000;

    /**
     * Keps track of the IDs currently in use to ensure uniqueness. Note that this is NOT intended to be iterated over,
     * instead we optimize for add/remove performance given the large initial capacity.
     *
     * This is a synchronized set to ensure multiple threads accessing this work correctly.
     */
    private static final Set<Integer> IDS_IN_USE = Collections.synchronizedSet(new HashSet<>(INITIAL_CAPACITY));

    /**
     * Generates a unique, random ID.
     *
     * @return a unique integer ID
     */
    public static int generateUniqueId() {
        int randomNum = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
        while (!IDS_IN_USE.add(randomNum)) {
            randomNum = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
        }
        return randomNum;
    }

    /**
     * See how many unique IDs are currently in use.
     *
     * @return number of unique IDs in use
     */
    public static int numUniqueIds() {
        return IDS_IN_USE.size();
    }

    /**
     * Retire an ID from use. Should be called in order to keep set size to a minimum.
     *
     * Initial naive implementation - should be automatic, ideally.
     *
     * @param id int ID to be removed
     * @return {@code true} if ID was in use, {@code false} otherwise
     */
    public static boolean retireId(final int id) {
        return IDS_IN_USE.remove(id);
    }
}
