package sd.p09.day83;

import java.time.Duration;

/**
 * @param retryAfter how long the caller should wait. Returning this rather than a bare 429 is the
 *                   difference between a client that backs off correctly and one that spins.
 */
public record RateLimitDecision(boolean allowed, long remaining, Duration retryAfter) {

    public static RateLimitDecision allow(long remaining) {
        return new RateLimitDecision(true, remaining, Duration.ZERO);
    }

    public static RateLimitDecision deny(Duration retryAfter) {
        return new RateLimitDecision(false, 0, retryAfter);
    }
}
