package sd.p01.day05;

import java.util.concurrent.atomic.AtomicLong;

/**
 * TODO(day05): lock-free, using {@code AtomicLong}.
 *
 * <p>No kernel involvement: a compare-and-swap instruction retries in user space until it
 * wins. Much faster than a lock at low-to-moderate contention. But every thread is
 * CAS-ing the SAME cache line, so at high contention the retries and the cache-line
 * ping-pong between cores start to dominate.
 */
public final class AtomicCounter implements Counter {

    AtomicLong atomicLong = new AtomicLong();

    @Override
    public void increment() {
        atomicLong.incrementAndGet();
    }

    @Override
    public long value() {
        return atomicLong.get();
    }

    @Override
    public String strategy() {
        return "AtomicLong";
    }
}
