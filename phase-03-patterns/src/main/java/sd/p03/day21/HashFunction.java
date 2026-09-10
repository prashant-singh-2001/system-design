package sd.p03.day21;

/**
 * The STRATEGY interface: one way of turning a key into a number, swappable at runtime.
 *
 * <p>Every implementation must be deterministic - the same key must always produce the same
 * hash, in this run and in every future one. A partitioning scheme built on a non-deterministic
 * or JVM-version-dependent hash (looking at you, {@code Object.hashCode()}'s identity fallback)
 * silently reshuffles every key the moment you restart the process.
 */
public interface HashFunction {

    /** A 32-bit hash, held as a {@code long} so callers never have to think about sign. */
    long hash(String key);
}
