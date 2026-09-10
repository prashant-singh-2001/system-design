package sd.p04.day39;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * TODO(day39): the storage primitive every earlier day quietly assumed existed - bounded,
 * TTL-aware, and correct about the two DIFFERENT ways real caches expire things.
 *
 * <p>Store, per key, both the value and its ABSOLUTE expiry {@link Instant} (or a sentinel
 * meaning "never expires" - {@code Instant.MAX} works well, since it can never be
 * {@code isBefore} or {@code equal to} any real "now"). A small private record for
 * {@code (value, expiresAt)} is the natural shape.
 *
 * <p><b>Two expiry mechanisms, both real, both needed:</b>
 * <ul>
 *   <li><b>Lazy, on read.</b> {@code get(key)} checks the entry's expiry against
 *       {@code clock.instant()}. If it has passed, remove the entry and return
 *       {@code Optional.empty()} - as if it were never there. This alone is not enough: a key
 *       nobody ever reads again would sit in memory forever, expired or not.</li>
 *   <li><b>Active sweeping.</b> {@code sweepExpired()} scans every entry and removes any that
 *       have expired, REGARDLESS of whether anyone has read them, returning how many were
 *       removed. This is what actually reclaims memory for keys nobody happens to touch again.
 *       {@code size()} should call this first, so it always reports the LIVE count rather than
     *       counting stale entries nobody has swept yet.</li>
 * </ul>
 *
 * <p><b>Capacity eviction</b> - when {@code put}/{@code putPermanent} would add a NEW key past
 * {@code capacity} (after sweeping, so you are not evicting to make room for space that a sweep
 * would have freed anyway): evict whichever currently-live entry has the SOONEST expiry - it was
 * already closest to being worthless. Treat permanent entries as expiring at {@code Instant.MAX}
 * so they are evicted only once no entry with a real TTL remains. (If every remaining entry is
 * permanent, which one gets evicted is intentionally unspecified - there is no expiry signal
 * left to decide with, which is itself worth noticing.) Updating an EXISTING key's value is
 * never a capacity event - it does not grow the entry count, so it can never trigger an eviction.
 */
public final class ExpiringStore<K, V> {

    private final Clock clock;
    private final int capacity;
    private final Map<K, Entry<V>> entries = new HashMap<>();

    public ExpiringStore(Clock clock, int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be at least 1");
        }
        this.clock = clock;
        this.capacity = capacity;
    }

    public void put(K key, V value, Duration ttl) {
        throw new UnsupportedOperationException(
                "TODO(day39): validate ttl is positive, then store with an absolute expiry");
    }

    public void putPermanent(K key, V value) {
        throw new UnsupportedOperationException("TODO(day39): store with expiresAt = Instant.MAX");
    }

    public Optional<V> get(K key) {
        throw new UnsupportedOperationException(
                "TODO(day39): lazily expire on read, else return the value");
    }

    public void remove(K key) {
        throw new UnsupportedOperationException("TODO(day39): implement remove");
    }

    public int size() {
        throw new UnsupportedOperationException("TODO(day39): sweep, then return the live count");
    }

    public int sweepExpired() {
        throw new UnsupportedOperationException(
                "TODO(day39): remove every entry whose expiry has passed, return how many");
    }

    private record Entry<V>(V value, Instant expiresAt) {
    }
}
