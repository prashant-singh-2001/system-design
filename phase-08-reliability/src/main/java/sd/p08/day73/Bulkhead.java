package sd.p08.day73;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO(day73): a bulkhead - the other half of failure isolation.
 *
 * <p>Named after a ship's compartments: a hull breach floods one section, not the whole vessel.
 *
 * <p>A circuit breaker reacts to failure. A bulkhead prevents one dependency from consuming
 * resources the others need, whether or not it is failing. A merely SLOW dependency - not erroring,
 * just slow - never trips a breaker, and can still occupy every thread you have.
 *
 * <p>Concretely: your service calls a recommendation API and a payments API from one thread pool.
 * Recommendations degrade to 10-second responses. Within a minute every thread is waiting on
 * recommendations, and payments - perfectly healthy - starts failing. You have coupled the
 * availability of your most important path to your least important one.
 *
 * <p>Give each dependency its own permit budget and that cannot happen. Recommendations saturate
 * their 10 permits and are refused; payments keeps its own. You have chosen, in advance, which
 * feature degrades under pressure - which is what "graceful degradation" actually means in code.
 *
 * <p>Implement {@code execute}: try to acquire a permit without blocking
 * ({@code semaphore.tryAcquire()}). If none is free, count a rejection and throw
 * {@link BulkheadFullException}. Otherwise run the action and <b>release the permit in a
 * finally block</b> - a leaked permit is a slow-motion outage that only appears under load.
 *
 * <p>Track the high-water mark of concurrent calls, so a test can prove the limit held.
 */
public final class Bulkhead {

    /** Thrown when a dependency has used its whole share of concurrency. */
    public static final class BulkheadFullException extends RuntimeException {
        public BulkheadFullException(String message) {
            super(message);
        }
    }

    private final String name;
    private final Semaphore permits;
    private final AtomicInteger inFlight = new AtomicInteger();
    private final AtomicInteger peakInFlight = new AtomicInteger();
    private final AtomicInteger rejected = new AtomicInteger();

    public Bulkhead(String name, int maxConcurrentCalls) {
        if (maxConcurrentCalls < 1) {
            throw new IllegalArgumentException("a bulkhead needs at least one permit");
        }
        this.name = name;
        this.permits = new Semaphore(maxConcurrentCalls);
    }

    public <T> T execute(Callable<T> action) {
        throw new UnsupportedOperationException("TODO(day73): tryAcquire, run, release in finally");
    }

    public int peakInFlight() {
        return peakInFlight.get();
    }

    public int rejected() {
        return rejected.get();
    }

    public String name() {
        return name;
    }
}
