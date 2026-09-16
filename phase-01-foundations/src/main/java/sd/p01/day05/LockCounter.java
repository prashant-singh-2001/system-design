package sd.p01.day05;

import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO(day05): the same guarantees using an explicit {@code ReentrantLock}.
 *
 * <p>Same cost profile as {@code synchronized}, but you gain {@code tryLock},
 * timeouts, interruptibility and fairness. Always release in a {@code finally}.
 *
 * <p>Worth knowing: a FAIR lock (new ReentrantLock(true)) hands ownership to the
 * longest waiter, which eliminates starvation and costs you an order of magnitude
 * in throughput. Fairness is rarely worth it.
 */
public final class LockCounter implements Counter {

    private long count;

    ReentrantLock lock = new ReentrantLock();

    @Override
    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public long value() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String strategy() {
        return "ReentrantLock";
    }
}
