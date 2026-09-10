package sd.p08.day73;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;

/**
 * TODO(day73): a circuit breaker, by hand.
 *
 * <p>Retries (Day 72) assume the problem is transient. A circuit breaker handles the case where it
 * is not. When a dependency is genuinely down, retrying is actively harmful: you burn your own
 * threads waiting on timeouts, and you add load to something already struggling.
 *
 * <p>The insight is counter-intuitive and worth holding on to: <b>failing fast is a feature.</b>
 * An immediate error lets you serve a degraded response - a cached value, a default, a partial
 * page - in microseconds. A timeout gives the user thirty seconds of nothing and then the same
 * error, having occupied a thread the whole time.
 *
 * <p>That thread occupation is the real danger. If every request to a dead dependency parks a
 * thread for its timeout, your pool fills with doomed calls and requests that had nothing to do
 * with that dependency start failing too. One sick service takes down a healthy one. A breaker
 * stops that propagation at the boundary.
 *
 * <p>The state machine:
 * <ul>
 *   <li><b>CLOSED</b> - calls pass through. Count consecutive failures; a success resets the
 *       count. At {@code failureThreshold}, trip to OPEN and record the time.</li>
 *   <li><b>OPEN</b> - throw {@link CircuitBreakerOpenException} immediately. Once
 *       {@code openDuration} has elapsed, move to HALF_OPEN on the next call and let it through.</li>
 *   <li><b>HALF_OPEN</b> - allow up to {@code halfOpenProbes} calls through. Any failure returns
 *       to OPEN (and resets the timer). {@code halfOpenProbes} consecutive successes close it and
 *       reset the counters.</li>
 * </ul>
 *
 * <p>{@code call} runs the supplied {@link Callable}, wrapping a checked exception in
 * {@link IllegalStateException}. Take the {@link Clock} as a constructor parameter - Day 20's
 * lesson - so the timing is testable without sleeping.
 *
 * <p>The trade-off to be honest about: a breaker fails requests that might have succeeded. Set
 * the threshold too low and a brief blip causes a self-inflicted outage; too high and it never
 * protects you. And a breaker per instance means each one learns independently, so a fleet of
 * fifty takes fifty times as many failures to react.
 */
public final class CircuitBreaker {

    private final int failureThreshold;
    private final Duration openDuration;
    private final int halfOpenProbes;
    private final Clock clock;

    private CircuitState state = CircuitState.CLOSED;
    private int consecutiveFailures;
    private int consecutiveProbeSuccesses;
    private Instant openedAt;
    private long rejectedCalls;

    public CircuitBreaker(int failureThreshold, Duration openDuration, int halfOpenProbes,
                          Clock clock) {
        if (failureThreshold < 1 || halfOpenProbes < 1) {
            throw new IllegalArgumentException("thresholds must be at least 1");
        }
        this.failureThreshold = failureThreshold;
        this.openDuration = openDuration;
        this.halfOpenProbes = halfOpenProbes;
        this.clock = clock;
    }

    public <T> T call(Callable<T> action) {
        throw new UnsupportedOperationException("TODO(day73): run the state machine");
    }

    public CircuitState state() {
        throw new UnsupportedOperationException("TODO(day73): report the current state");
    }

    /** How many calls were refused without reaching the dependency. */
    public long rejectedCalls() {
        return rejectedCalls;
    }
}
