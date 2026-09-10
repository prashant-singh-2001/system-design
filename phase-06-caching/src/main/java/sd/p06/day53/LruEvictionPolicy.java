package sd.p06.day53;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TODO(day53): evict the LEAST RECENTLY USED key.
 *
 * <p>The bet: what was used recently will be used again. That is true for most workloads, which
 * is why LRU is the default nearly everywhere.
 *
 * <p>Its weakness is worth knowing: a single large scan - a backup job, an analytics query
 * walking every row - touches everything once and flushes your genuinely hot data out. That is
 * called <b>cache pollution</b>, and it is why real caches use variants like LRU-K or
 * segmented LRU.
 *
 * <p>A {@code LinkedHashMap} with {@code accessOrder = true} maintains the order for you: the
 * eldest entry (the first key in iteration order) is the least recently used.
 */
public final class LruEvictionPolicy implements EvictionPolicy {

    private final Map<String, Boolean> recency = new LinkedHashMap<>(16, 0.75f, true);

    @Override
    public void recordInsert(String key) {
        throw new UnsupportedOperationException("TODO(day53): track the new key as most recent");
    }

    @Override
    public void recordAccess(String key) {
        throw new UnsupportedOperationException("TODO(day53): move the key to most recent");
    }

    @Override
    public void remove(String key) {
        throw new UnsupportedOperationException("TODO(day53): stop tracking the key");
    }

    @Override
    public String evictionCandidate() {
        throw new UnsupportedOperationException("TODO(day53): the eldest key, or null if empty");
    }

    @Override
    public String name() {
        return "LRU";
    }
}
