package sd.p05.day47;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * TODO(day47): the WRITE-optimised half of an LSM tree - everything recent lives here, in
 * memory, sorted by key. A write is just inserting into a sorted map: no disk seek, no
 * in-place update of an existing page, which is the entire reason LSM trees can absorb writes
 * so much faster than a B-tree updating in place.
 *
 * <p>Use a {@link TreeMap} so {@link #entries()} can hand back entries in sorted key order for
 * free - {@link SSTable#flush} needs that ordering to write a sorted file.
 */
public final class MemTable {

    private final TreeMap<String, String> data = new TreeMap<>();

    public void put(String key, String value) {
        throw new UnsupportedOperationException("TODO(day47): implement put");
    }

    public Optional<String> get(String key) {
        throw new UnsupportedOperationException("TODO(day47): implement get");
    }

    public int size() {
        throw new UnsupportedOperationException("TODO(day47): implement size");
    }

    /** Every entry, in ascending key order. */
    public Iterable<Map.Entry<String, String>> entries() {
        throw new UnsupportedOperationException("TODO(day47): return data.entrySet()");
    }
}
