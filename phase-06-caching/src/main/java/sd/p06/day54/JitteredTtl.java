package sd.p06.day54;

import java.time.Duration;
import java.util.random.RandomGenerator;

/**
 * TODO(day54): the second fix - stop everything expiring at the same instant.
 *
 * <p>Single-flight fixes ONE hot key. It does nothing about the other stampede: a deploy or a
 * cache flush populates ten thousand keys within the same second, all with a 60-minute TTL, so
 * an hour later ten thousand keys expire together. Every one of them is a separate key, so
 * single-flight - which only deduplicates per key - does not help at all.
 *
 * <p>The fix is to spread the deadlines. Given a base TTL and a jitter fraction, return a
 * duration drawn uniformly from
 * {@code [base x (1 - jitter), base x (1 + jitter)]}. With a 60-minute base and 0.2 jitter,
 * expiries scatter across a 24-minute window instead of landing on one second.
 *
 * <p>This is the same idea as jittered retry backoff, which you will build on Day 72. Whenever
 * many independent actors share a deadline, add noise - synchronised clients are a load spike
 * waiting to happen.
 *
 * <p>Take the {@link RandomGenerator} as a parameter so tests can pass a seeded one. Validate:
 * jitter must be in [0, 1], and a base TTL must be positive.
 */
public final class JitteredTtl {

    private JitteredTtl() {
    }

    public static Duration jitter(Duration base, double jitterFraction, RandomGenerator random) {
        throw new UnsupportedOperationException("TODO(day54): spread the deadline");
    }
}
