package sd.p01.day05;

/**
 * GIVEN - the reference implementation, so you can see the shape.
 *
 * <p>Correct and simple. Under contention the JVM inflates the lock and threads park in the
 * kernel, so a contended increment can cost microseconds rather than nanoseconds.
 */
public final class SynchronizedCounter implements Counter {

    private long count;

    @Override
    public synchronized void increment() {
        count++;
    }

    @Override
    public synchronized long value() {
        return count;
    }

    @Override
    public String strategy() {
        return "synchronized";
    }
}
