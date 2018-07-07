package org.dreesbach.ticketing.id;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Interface for generating unique IDs.
 */
public interface IdGenerator {
    /**
     * Atomic counter to make generated IDs unique.
     */
    AtomicInteger COUNTER = new AtomicInteger();

    /**
     * Generates a unique ID based on a hash code of the email passed in plus an atomic counter.
     *
     * @param email email address of the customer
     * @return a unique integer ID
     */
    static int generateUniqueId(final String email) {
        return 1 + COUNTER.getAndAdd(1);
    }
}
