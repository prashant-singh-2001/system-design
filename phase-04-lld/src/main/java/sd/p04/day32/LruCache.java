package sd.p04.day32;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO(day32): the classic - O(1) {@code get} and {@code put}, LRU eviction, and this time,
 * genuinely thread-safe.
 *
 * <p>The O(1) shape needs two structures working together:
 * <ul>
 *   <li>a {@code HashMap<K, Node>} for O(1) lookup by key</li>
 *   <li>an INTRUSIVE doubly-linked list threading those same {@link Node} objects together in
 *       recency order, so moving an entry to "most recently used" is a pointer splice, not a
 *       search. "Intrusive" means the links live INSIDE {@code Node} itself, not in a separate
 *       {@code LinkedList} wrapper - which is what makes the splice O(1) instead of O(n).</li>
 * </ul>
 *
 * <p>Use sentinel {@code head} and {@code tail} nodes (holding no real key/value) so every real
 * node always has a non-null {@code prev} and {@code next} - no null-checks at the boundaries.
 * Keep the list ordered LEAST-recently-used nearest {@code head}, MOST-recently-used nearest
 * {@code tail} (or the reverse - just be consistent, since it only matters that you know which
 * end to evict from).
 *
 * <ul>
 *   <li>{@code get(key)}: if present, unlink the node from its current position and re-link it
 *       at the MRU end, then return its value. A read is also a write to the recency order -
 *       that is precisely why this needs a lock, not just {@code put}.</li>
 *   <li>{@code put(key, value)}: if the key exists, update its value and move it to the MRU end.
 *       If new and the map is at capacity, evict the LRU end's node (remove from both the map
 *       and the list) before inserting the new one at the MRU end.</li>
 * </ul>
 *
 * <p>For thread safety today, wrap every operation - {@code get} included - in a single {@code
 * ReentrantLock}. This is a coarse-grained lock: correct, and contended under heavy concurrent
 * load, because a read blocks every other read and write. Real production caches (Caffeine,
 * Guava) split the map into segments with independent locks for exactly this reason - the
 * brief's reflect section asks you to reason about that trade rather than build it today.
 */
public final class LruCache<K, V> {

    private final int capacity;
    private final Map<K, Node<K, V>> index = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    private final Node<K, V> head = new Node<>(null, null);
    private final Node<K, V> tail = new Node<>(null, null);

    public LruCache(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be at least 1");
        }
        this.capacity = capacity;
        head.next = tail;
        tail.prev = head;
    }

    public Optional<V> get(K key) {
        throw new UnsupportedOperationException(
                "TODO(day32): lock; look up; if present, move to MRU end; return the value");
    }

    public void put(K key, V value) {
        throw new UnsupportedOperationException(
                "TODO(day32): lock; update-and-move, or insert-and-evict-if-full");
    }

    public int size() {
        throw new UnsupportedOperationException("TODO(day32): lock; return index.size()");
    }

    /** An intrusive node: the linked-list pointers live on the object itself. */
    private static final class Node<K, V> {
        final K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
