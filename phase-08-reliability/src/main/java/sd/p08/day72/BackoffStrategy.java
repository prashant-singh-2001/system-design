package sd.p08.day72;

import java.time.Duration;
import java.util.random.RandomGenerator;

/**
 * TODO(day72): how long to wait before retry number n.
 *
 * <p>Retries are the most dangerous reliability feature you can add, because they are the only one
 * that makes an overload <b>worse</b>. A dependency slows down, every client retries, load
 * triples, the dependency slows further, and you have built a positive feedback loop into your
 * own architecture. That is a <b>retry storm</b>, and it is how a brief degradation becomes a
 * total outage.
 *
 * <p>Three strategies, each fixing the previous one's flaw:
 *
 * <ul>
 *   <li><b>{@code fixed}</b> - the same delay every time. Simple, and it keeps hammering at a
 *       constant rate, so it does nothing to relieve the thing that is struggling.</li>
 *   <li><b>{@code exponential}</b> - {@code base x 2^(attempt-1)}, capped at {@code max}. Backs
 *       off fast, giving the dependency room. But every client that failed at the same moment
 *       retries at the same moment, again and again - synchronised waves of load.</li>
 *   <li><b>{@code fullJitter}</b> - a uniform random draw from
 *       {@code [0, exponential(attempt)]}. This is the one to remember. It keeps the exponential
 *       backoff and destroys the synchronisation, spreading retries smoothly instead of in
 *       spikes.</li>
 * </ul>
 *
 * <p>AWS's published analysis is worth knowing: full jitter beats both no-jitter and
 * "half base plus half random" on total work done and on completion time. Randomness is not a
 * tweak here - it is the mechanism.
 *
 * <p>You already met this idea on Day 54 as jittered cache TTLs. Whenever many independent actors
 * share a deadline, add noise.
 *
 * <p>Attempts are 1-based. Validate: attempt >= 1, positive base, max not less than base.
 */
public final class BackoffStrategy {

    private BackoffStrategy() {
    }

    public static Duration fixed(Duration delay, int attempt) {
        throw new UnsupportedOperationException("TODO(day72): the same delay every time");
    }

    public static Duration exponential(Duration base, Duration max, int attempt) {
        throw new UnsupportedOperationException("TODO(day72): base x 2^(attempt-1), capped");
    }

    public static Duration fullJitter(Duration base, Duration max, int attempt,
                                      RandomGenerator random) {
        throw new UnsupportedOperationException("TODO(day72): uniform in [0, exponential]");
    }
}
