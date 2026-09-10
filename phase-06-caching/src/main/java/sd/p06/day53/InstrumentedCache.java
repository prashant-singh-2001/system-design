package sd.p06.day53;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * TODO(day53): a bounded cache with TTL that reports on itself.
 *
 * <p>Three mechanisms, and they are genuinely different things:
 * <ul>
 *   <li><b>Capacity</b> bounds MEMORY. When full, the {@link EvictionPolicy} picks a victim.</li>
 *   <li><b>TTL</b> bounds STALENESS. An entry past its deadline is gone regardless of space.</li>
 *   <li><b>Stats</b> tell you whether any of it is working.</li>
 * </ul>
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code get} - an absent key is a miss. An expired key is an <b>expiration and a miss</b>,
 *       and must be removed from both the map and the policy. Otherwise it is a hit: record the
 *       access with the policy and return the value.</li>
 *   <li>{@code put} - overwriting an existing key must NOT evict anything. Only a genuinely new
 *       key that would exceed capacity triggers an eviction, and the eviction happens BEFORE the
 *       insert so the cache never exceeds its bound even momentarily.</li>
 *   <li>{@code stats} - a snapshot of the four counters.</li>
 * </ul>
 *
 * <p>Store an expiry {@link Instant} per entry rather than an insertion time; the check is then
 * a single comparison against {@code clock.instant()} with no arithmetic to get wrong at the
 * boundary. Treat an entry as expired when now is at or after its deadline.
 */
public final class InstrumentedCache {

    private record Entry(String value, Instant expiresAt) {
    }

    private final Map<String, Entry> entries = new HashMap<>();
    private final int capacity;
    private final Duration ttl;
    private final Clock clock;
    private final EvictionPolicy policy;

    private long hits;
    private long misses;
    private long evictions;
    private long expirations;

    public InstrumentedCache(int capacity, Duration ttl, Clock clock, EvictionPolicy policy) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be at least 1");
        }
        this.capacity = capacity;
        this.ttl = ttl;
        this.clock = clock;
        this.policy = policy;
    }

    public Optional<String> get(String key) {
        throw new UnsupportedOperationException("TODO(day53): miss, expiration, or hit");
    }

    public void put(String key, String value) {
        throw new UnsupportedOperationException("TODO(day53): evict if needed, then insert");
    }

    public int size() {
        return entries.size();
    }

    public CacheStats stats() {
        return new CacheStats(hits, misses, evictions, expirations);
    }

    public String policyName() {
        return policy.name();
    }
}
