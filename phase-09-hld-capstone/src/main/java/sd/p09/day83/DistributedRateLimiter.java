package sd.p09.day83;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO(day83): the sliding-window log, and why the naive fixed window is not good enough.
 *
 * <p>A <b>fixed window</b> counter - "100 requests per minute", reset on the minute - is trivial
 * and has a boundary flaw that doubles your limit. A client sends 100 requests at 11:59:59 and 100
 * more at 12:00:01: two hundred requests in two seconds, both windows technically satisfied. If
 * your limit exists to protect a downstream service, it just failed at exactly the moment it
 * mattered.
 *
 * <p>A <b>sliding-window log</b> keeps the timestamp of each request and counts those within the
 * window ending now. Exact, no boundary artefact, and the cost is memory proportional to the
 * limit - which is why production systems often use a sliding-window *counter* that interpolates
 * between two fixed windows: approximate, and constant memory.
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code check(clientId)} - evict timestamps older than the window, then allow if the
 *       remaining count is below the limit (recording this request), or deny.</li>
 *   <li>On denial, {@code retryAfter} is the time until the OLDEST request in the window falls
 *       out of it - the earliest moment a slot frees. Returning that number is what lets a
 *       well-behaved client back off precisely instead of guessing.</li>
 * </ul>
 *
 * <p>The distributed part is the design question rather than the code question, and it is what
 * Day 83's document is about: with N app servers, a per-instance limiter allows N times your
 * intended rate. The options are a shared store (Redis, exact, one network hop per request),
 * or per-instance limits at {@code limit/N} (free, and wrong whenever traffic is unevenly
 * balanced or an instance is down).
 */
public final class DistributedRateLimiter {

    private final Map<String, Deque<Instant>> requestLog = new HashMap<>();
    private final int limit;
    private final Duration window;
    private final Clock clock;

    public DistributedRateLimiter(int limit, Duration window, Clock clock) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be at least 1");
        }
        this.limit = limit;
        this.window = window;
        this.clock = clock;
    }

    public RateLimitDecision check(String clientId) {
        throw new UnsupportedOperationException("TODO(day83): evict, count, decide, report retry-after");
    }

    /** How many timestamps are currently retained for this client. Memory is the cost of exactness. */
    public int trackedRequests(String clientId) {
        Deque<Instant> log = requestLog.get(clientId);
        return log == null ? 0 : log.size();
    }
}
