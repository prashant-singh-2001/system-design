package sd.p06.day56;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * TODO(day56): consistent hashing - the fix for the problem you met on Day 49.
 *
 * <p><b>The problem.</b> {@code hash(key) % N} distributes beautifully and reshards horribly.
 * Go from 4 nodes to 5 and roughly 80% of keys change owner, because {@code h % 4} and
 * {@code h % 5} disagree for almost every h. For a cache that means a near-total cold start at
 * the exact moment you were adding capacity because you were under load. Systems have died this
 * way.
 *
 * <p><b>The idea.</b> Map both nodes and keys onto the same circular hash space. A key belongs
 * to the first node encountered walking clockwise from the key's position. Add a node and it
 * claims only the arc between itself and its predecessor - roughly {@code 1/N} of the keys.
 * Every other key stays exactly where it was.
 *
 * <p><b>Virtual nodes.</b> With one point per node the arcs are wildly uneven - random points on
 * a circle cluster, so one node ends up owning far more than its share. Place each node at
 * {@code virtualNodesPerNode} positions instead (hash {@code "nodeA#0"}, {@code "nodeA#1"}, ...)
 * and the law of large numbers evens it out. 150-200 virtual nodes is the usual range. This also
 * gives you weighting for free: a machine with twice the memory gets twice the virtual nodes.
 *
 * <p>Implement with a {@link TreeMap} from hash position to node name:
 * <ul>
 *   <li>{@code addNode} - insert every virtual node's position</li>
 *   <li>{@code removeNode} - remove them all</li>
 *   <li>{@code nodeFor} - {@code tailMap(hash)}: the first entry at or after the key's position,
 *       wrapping to {@code firstKey()} when the key falls past the last point. That wrap is what
 *       makes it a ring; forget it and every key beyond the highest position throws.</li>
 *   <li>{@code keyDistribution} - how many of the given keys land on each node</li>
 * </ul>
 *
 * <p>Use {@link #hash(String)} for both nodes and keys, so they share one space.
 *
 * <p>An empty ring has no owner: {@code nodeFor} must throw {@link IllegalStateException} rather
 * than return null, because a silent null here becomes a NullPointerException three frames away.
 */
public final class ConsistentHashRing {

    private final SortedMap<Long, String> ring = new TreeMap<>();
    private final int virtualNodesPerNode;

    public ConsistentHashRing(int virtualNodesPerNode) {
        if (virtualNodesPerNode < 1) {
            throw new IllegalArgumentException("need at least one virtual node per node");
        }
        this.virtualNodesPerNode = virtualNodesPerNode;
    }

    public void addNode(String node) {
        throw new UnsupportedOperationException("TODO(day56): place its virtual nodes on the ring");
    }

    public void removeNode(String node) {
        throw new UnsupportedOperationException("TODO(day56): remove its virtual nodes");
    }

    public String nodeFor(String key) {
        throw new UnsupportedOperationException("TODO(day56): first node clockwise, wrapping round");
    }

    public Map<String, Integer> keyDistribution(Collection<String> keys) {
        throw new UnsupportedOperationException("TODO(day56): count keys per node");
    }

    public int ringSize() {
        return ring.size();
    }

    // ---------------------------------------------------------------- given

    /**
     * FNV-1a, widened to a positive long. Day 21's hash function, reused - the ring needs a
     * well-distributed hash and {@code String.hashCode()} clusters badly on similar keys.
     */
    public static long hash(String value) {
        long hash = 2166136261L;
        for (byte b : value.getBytes(java.nio.charset.StandardCharsets.UTF_8)) {
            hash ^= (b & 0xffL);
            hash *= 16777619L;
        }
        return hash & 0x7fffffffffffffffL;
    }

    /** GIVEN - what fraction of these keys move when going from one ring to another. */
    public static double keyMovementFraction(ConsistentHashRing before, ConsistentHashRing after,
                                             List<String> keys) {
        long moved = keys.stream()
                .filter(key -> !before.nodeFor(key).equals(after.nodeFor(key)))
                .count();
        return (double) moved / keys.size();
    }
}
