package sd.p04.day33;

import java.time.Clock;

/**
 * TODO(day33): a bucket holds up to {@code capacity} tokens; it refills continuously at
 * {@code refillTokensPerSecond}; every call that finds at least one token consumes exactly one
 * and is allowed. This is the algorithm behind almost every "N requests per second, with bursts
 * allowed" limiter you have configured without necessarily naming it.
 *
 * <p>State: a token COUNT (a {@code double} - refill happens continuously, not in whole-token
 * jumps, so fractional tokens are real and matter) and the timestamp of the last refill.
 *
 * <p>{@code tryAcquire()}, precisely:
 * <ol>
 *   <li>compute elapsed time since the last refill, using the injected {@link Clock}</li>
 *   <li>add {@code elapsedSeconds * refillTokensPerSecond} tokens, capped at {@code capacity} -
 *       the cap matters: without it, an idle bucket over a long enough period would accumulate
     *       an unbounded backlog and then release it all as one enormous burst</li>
 *   <li>update the last-refill timestamp to now</li>
 *   <li>if at least one token is available, subtract one and return {@code true}; otherwise
 *       return {@code false} and leave the (fractional) token count untouched</li>
 * </ol>
 *
 * <p>The defining behaviour this buys you: an idle bucket lets an initial BURST of up to
 * {@code capacity} requests through instantly, then settles into throttling at the steady
 * refill rate - which is usually exactly what you want for a client that is bursty but
 * well-behaved on average.
 */
public final class TokenBucketRateLimiter implements RateLimiter {

    private final long capacity;
    private final double refillTokensPerSecond;
    private final Clock clock;

    public TokenBucketRateLimiter(long capacity, double refillTokensPerSecond, Clock clock) {
        throw new UnsupportedOperationException(
                "TODO(day33): validate positive capacity/rate, store; START FULL (tokens = capacity)");
    }

    @Override
    public boolean tryAcquire() {
        throw new UnsupportedOperationException(
                "TODO(day33): refill based on elapsed time, cap at capacity, consume if >= 1");
    }
}
