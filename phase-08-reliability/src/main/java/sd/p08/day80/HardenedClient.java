package sd.p08.day80;

import sd.p08.day72.BackoffStrategy;
import sd.p08.day72.RetryBudget;
import sd.p08.day73.Bulkhead;
import sd.p08.day73.CircuitBreaker;
import sd.p08.day73.CircuitBreakerOpenException;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.random.RandomGenerator;

/**
 * TODO(day80): the phase capstone - every defence from days 71-79, composed, in the right order.
 *
 * <p>Each mechanism alone is incomplete. Retries without a breaker hammer a dead service. A
 * breaker without a bulkhead lets a merely SLOW dependency exhaust your threads. Neither helps if
 * retrying corrupts data, which is what idempotency is for. Reliability is a stack, and the order
 * matters:
 *
 * <pre>
 *   bulkhead  -> is there capacity for this dependency at all?   (isolate)
 *     breaker -> do we believe it is up?                          (fail fast)
 *       retry -> transient failure? back off, jittered, on budget (recover)
 *         call
 * </pre>
 *
 * <p>Outermost is the cheapest rejection. A bulkhead rejection costs a semaphore check; a breaker
 * rejection costs a state read; a retry costs real time. Reject as early and as cheaply as you
 * can - and note that the ordering also means a retry storm cannot bypass the bulkhead, which it
 * could if you nested them the other way.
 *
 * <p>Implement {@code call(input)}:
 * <ol>
 *   <li>Run the whole thing inside {@code bulkhead.execute(...)}.</li>
 *   <li>Inside that, loop up to {@code maxAttempts}:
 *     <ul>
 *       <li>Call the dependency through {@code breaker.call(...)}. On success record it with the
 *           retry budget and return the value.</li>
 *       <li>On {@link CircuitBreakerOpenException}, stop immediately - do NOT retry. The breaker
 *           has already decided the dependency is down; retrying against an open breaker is
 *           pointless work and defeats the purpose of having one.</li>
 *       <li>On any other failure: if attempts remain AND the retry budget allows it, sleep for
 *           {@code BackoffStrategy.fullJitter(...)} and try again. Otherwise give up.</li>
 *     </ul>
 *   </li>
 *   <li>Return {@code Optional.empty()} when every path is exhausted, so the caller can degrade
 *       gracefully rather than propagate an exception. That choice is the point of the whole
 *       stack: a bounded, fast, predictable failure the caller can plan for.</li>
 * </ol>
 *
 * <p>Record a rejection whenever you return empty, so the tests can see which defence fired.
 */
public final class HardenedClient {

    private final FlakyDependency dependency;
    private final Bulkhead bulkhead;
    private final CircuitBreaker breaker;
    private final RetryBudget budget;
    private final RandomGenerator random;
    private final int maxAttempts;

    private final AtomicInteger degradedResponses = new AtomicInteger();
    private final AtomicInteger successes = new AtomicInteger();

    public HardenedClient(FlakyDependency dependency, Bulkhead bulkhead, CircuitBreaker breaker,
                          RetryBudget budget, int maxAttempts, RandomGenerator random) {
        this.dependency = dependency;
        this.bulkhead = bulkhead;
        this.breaker = breaker;
        this.budget = budget;
        this.maxAttempts = maxAttempts;
        this.random = random;
    }

    public Optional<String> call(String input) {
        throw new UnsupportedOperationException("TODO(day80): bulkhead, breaker, retry, degrade");
    }

    public int successes() {
        return successes.get();
    }

    public int degradedResponses() {
        return degradedResponses.get();
    }

    // ---------------------------------------------------------------- given

    static Duration backoffFor(int attempt, RandomGenerator random) {
        return BackoffStrategy.fullJitter(
                Duration.ofMillis(5), Duration.ofMillis(50), attempt, random);
    }
}
