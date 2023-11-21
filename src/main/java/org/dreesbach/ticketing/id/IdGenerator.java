package org.dreesbach.ticketing.id;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Implementation of the IdGenerator interface.
 */
public final class IdGenerator {

    /**
     * Maximum length of reservation codes to generate. At 6 characters we get ~2.1 billion unique codes, which should
     * suffice for this implementation. For a more complete production implementation we'd want to keep track of these in
     * maybe a key-value store like Redis, Cassandra, or Couchbase (with possibly an IMDG like Hazelcast or a simple
     * cache like memcached wrapping one of these to improve speed).
     */
    public static final int MAX_RESERVATION_CODE_LENGTH = 6;
    /**
     * Max number of calls to the {@link rng} to allow before re-seeding it.
     */
    private static final int MAX_CALL_COUNT_BEFORE_RESET = 10_000;
    /**
     * Random number generator for this class.
     */
    private static SecureRandom rng = new SecureRandom();
    /**
     * Keeps track of the number of times the {@link rng} was called.
     */
    private static int callCounter = 0;

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
     * Same as IDS_IN_USE, but to track reservation IDs.
     */
    private static final Set<String> RESERVATION_IDS_IN_USE = Collections.synchronizedSet(new HashSet<>(INITIAL_CAPACITY));

    /**
     * Generates a unique, random ID.
     *
     * @return a unique integer ID
     */
    public static int generateUniqueIntId() {
        reseedRng();
        int randomNum = rng.nextInt(Integer.MAX_VALUE);
        synchronized (IDS_IN_USE) {
            while (!IDS_IN_USE.add(randomNum)) {
                randomNum = rng.nextInt(Integer.MAX_VALUE);
            }
        }
        return randomNum;
    }

    /**
     * Generate a unique reservation code.
     *
     * @return a {@value MAX_RESERVATION_CODE_LENGTH} character string for the reservation code
     */
    public static String generateReservationCode() {
        reseedRng();
        String reservationCode = internalGenerateReservationCode();
        synchronized (RESERVATION_IDS_IN_USE) {
            while (!RESERVATION_IDS_IN_USE.add(reservationCode)) {
                reservationCode = internalGenerateReservationCode();
            }
        }
        return reservationCode;
    }

    /**
     * Internal method for generating a reservation code.
     *
     * This could be improved by excluding potentially confusing characters like 1 and I, 0 and O, etc., but we won't worry
     * about that for this simple implementation for now.
     *
     * There are a fair number of short-lived objects created by this that may create some GC pressure, but I will leave that
     * worry out of scope for now.
     *
     * @return a {@value MAX_RESERVATION_CODE_LENGTH} character unique string
     */
    private static String internalGenerateReservationCode() {
        byte[] bytes = new byte[MAX_RESERVATION_CODE_LENGTH + 1];
        rng.nextBytes(bytes);
        BigInteger bigInteger = new BigInteger(bytes).abs();
        return bigInteger
                .toString(Character.MAX_RADIX)
                .toUpperCase(Locale.getDefault())
                .substring(0, MAX_RESERVATION_CODE_LENGTH);
    }

    /**
     * See how many unique IDs are currently in use.
     *
     * @return number of unique IDs in use
     */
    public static int numUniqueIds() {
        synchronized (IDS_IN_USE) {
            return IDS_IN_USE.size();
        }
    }

    /**
     * See how many unique reservation IDs are currently in use.
     *
     * @return number of unique reservation IDs in use
     */
    public static int numUniqueReservationIds() {
        return RESERVATION_IDS_IN_USE.size();
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
        synchronized (IDS_IN_USE) {
            return IDS_IN_USE.remove(id);
        }
    }

    /**
     * Retire a reservation ID from use. Should be called in order to keep set size to a minimum.
     *
     * Initial naive implementation - should be automatic, ideally.
     *
     * @param id String ID to be removed
     * @return {@code true} if ID was in use, {@code false} otherwise
     */
    public static boolean retireReservationId(final String id) {
        synchronized (RESERVATION_IDS_IN_USE) {
            return RESERVATION_IDS_IN_USE.remove(id);
        }
    }

    /**
     * Utility method to re-seed the {@link rng}.
     */
    private static void reseedRng() {
        if (callCounter % MAX_CALL_COUNT_BEFORE_RESET == 0) {
            rng.setSeed(Instant.now().getEpochSecond());
            callCounter = 0;
        }
    }
}
