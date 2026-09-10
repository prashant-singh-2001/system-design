package sd.p01.day05;

/**
 * TODO(day05): lock-free, using {@code AtomicLong}.
 *
 * <p>No kernel involvement: a compare-and-swap instruction retries in user space until it
 * wins. Much faster than a lock at low-to-moderate contention. But every thread is
 * CAS-ing the SAME cache line, so at high contention the retries and the cache-line
 * ping-pong between cores start to dominate.
 */
public final class AtomicCounter implements Counter {

    @Override
    public void increment() {
        throw new UnsupportedOperationException("TODO(day05): use an AtomicLong");
    }

    @Override
    public long value() {
        throw new UnsupportedOperationException("TODO(day05): use an AtomicLong");
    }

    @Override
    public String strategy() {
        return "AtomicLong";
    }
}
