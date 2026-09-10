package sd.p01.day04;

/**
 * GIVEN, and deliberately broken. Do not fix this one.
 *
 * <p>{@code count++} is three operations: read, add one, write back. Two threads can read
 * the same value, both add one, and both write back the same result. One increment vanishes.
 * The JVM is allowed to do this - nothing here establishes a happens-before relationship.
 */
public final class UnsafeCounter implements Counter {

    private long count;

    @Override
    public void increment() {
        count++;
    }

    @Override
    public long value() {
        return count;
    }
}
