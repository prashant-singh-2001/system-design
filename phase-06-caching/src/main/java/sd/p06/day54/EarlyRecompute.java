package sd.p06.day54;

import java.time.Duration;
import java.util.random.RandomGenerator;

/**
 * TODO(day54): the third fix - probabilistic early expiry, sometimes called XFetch.
 *
 * <p>The idea: rather than everyone discovering an expiry at once, let each reader decide,
 * independently and at random, to refresh a value that is merely getting OLD. As an entry
 * approaches its deadline the probability rises, so almost certainly somebody refreshes it
 * before it ever actually expires - and the expiry moment, when nobody is holding a value,
 * simply never arrives.
 *
 * <p>A simple, honest version - and the one to implement:
 *
 * <pre>
 *   fractionElapsed = age / ttl
 *   if (fractionElapsed &lt; beta)  -&gt; never refresh early
 *   else refresh with probability (fractionElapsed - beta) / (1 - beta)
 * </pre>
 *
 * <p>With {@code beta = 0.8}: nothing happens for the first 80% of the entry's life, then the
 * chance ramps linearly from 0 to 1 over the final 20%. Return {@code true} when this particular
 * reader should do the refresh.
 *
 * <p>An entry at or past its TTL always returns {@code true} - it is expired, so somebody must.
 *
 * <p>The trade-off: you do slightly more work than strictly necessary - some refreshes happen
 * before they had to - in exchange for the p99 never containing a load. That is usually a very
 * good deal on a hot key, and a waste on a cold one.
 *
 * <p>Validate: beta in [0, 1), positive ttl, non-negative age.
 */
public final class EarlyRecompute {

    private EarlyRecompute() {
    }

    public static boolean shouldRefreshEarly(Duration age, Duration ttl, double beta,
                                             RandomGenerator random) {
        throw new UnsupportedOperationException("TODO(day54): probabilistic early refresh");
    }
}
