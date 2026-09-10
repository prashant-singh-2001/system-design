package sd.p06.day53;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO(day53): evict the LEAST FREQUENTLY USED key.
 *
 * <p>The bet: what has been popular will stay popular. It resists the scan that defeats LRU,
 * because one touch does not outrank a thousand.
 *
 * <p>Its own weakness is the mirror image: a key that was enormously popular last week keeps its
 * high count and squats in the cache forever, while a genuinely rising key cannot displace it.
 * Real LFU implementations add decay for exactly this reason.
 *
 * <p>Count every insert and access. On a tie, evict the key that is smallest by natural string
 * order - not because that is meaningful, but because a deterministic tie-break makes the
 * behaviour testable, and untestable eviction is how caches become mysterious.
 */
public final class LfuEvictionPolicy implements EvictionPolicy {

    private final Map<String, Long> frequency = new HashMap<>();

    @Override
    public void recordInsert(String key) {
        throw new UnsupportedOperationException("TODO(day53): start this key's count at 1");
    }

    @Override
    public void recordAccess(String key) {
        throw new UnsupportedOperationException("TODO(day53): increment this key's count");
    }

    @Override
    public void remove(String key) {
        throw new UnsupportedOperationException("TODO(day53): stop tracking the key");
    }

    @Override
    public String evictionCandidate() {
        throw new UnsupportedOperationException(
                "TODO(day53): lowest count, ties broken by smallest key; null if empty");
    }

    @Override
    public String name() {
        return "LFU";
    }
}
