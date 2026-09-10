package sd.p04.day33;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * TODO(day33): the OTHER classic - track the timestamp of every recent request in a deque, and
 * allow a new one only if fewer than {@code maxRequests} timestamps remain inside the trailing
 * {@code window}. Precise, at the cost of remembering every recent request rather than a single
 * running count.
 *
 * <p>{@code tryAcquire()}, precisely:
 * <ol>
 *   <li>let {@code now = clock.instant()} and {@code cutoff = now.minus(window)}</li>
 *   <li>evict every timestamp at the front of the deque that is at or before {@code cutoff} -
 *       they are outside the trailing window and no longer count against the limit</li>
 *   <li>if the deque's remaining size is {@code < maxRequests}, record {@code now} at the back
 *       of the deque and return {@code true}; otherwise return {@code false} and record
 *       nothing</li>
 * </ol>
 *
 * <p>Unlike a token bucket, this scheme has NO smooth refill - capacity only frees up as
 * individual old requests age past the window. If {@code maxRequests} requests all land in a
 * tight burst at the start of a window, none of that capacity returns until each of THOSE
 * specific timestamps individually falls outside the window - which, for a burst that happened
 * all at once, means waiting for nearly the FULL window duration before any of it does.
 */
public final class SlidingWindowRateLimiter implements RateLimiter {

    private final int maxRequests;
    private final Duration window;
    private final Clock clock;
    private final Deque<java.time.Instant> timestamps = new ArrayDeque<>();

    public SlidingWindowRateLimiter(int maxRequests, Duration window, Clock clock) {
        throw new UnsupportedOperationException(
                "TODO(day33): validate maxRequests > 0 and a positive window, store the rest");
    }

    @Override
    public boolean tryAcquire() {
        throw new UnsupportedOperationException(
                "TODO(day33): evict timestamps outside the window, then admit if under the limit");
    }
}
