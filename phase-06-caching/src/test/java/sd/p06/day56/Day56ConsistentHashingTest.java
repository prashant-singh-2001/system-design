package sd.p06.day56;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day56ConsistentHashingTest {

    private static final int VIRTUAL_NODES = 200;

    private static List<String> keys(int count) {
        List<String> keys = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            keys.add("key-" + i);
        }
        return keys;
    }

    private static ConsistentHashRing ringOf(String... nodes) {
        ConsistentHashRing ring = new ConsistentHashRing(VIRTUAL_NODES);
        for (String node : nodes) {
            ring.addNode(node);
        }
        return ring;
    }

    @Test
    @DisplayName("the same key always maps to the same node")
    void deterministic() {
        ConsistentHashRing ring = ringOf("a", "b", "c");

        String owner = ring.nodeFor("user:42");
        for (int i = 0; i < 100; i++) {
            assertThat(ring.nodeFor("user:42")).isEqualTo(owner);
        }
    }

    @Test
    @DisplayName("every node gets its virtual points on the ring")
    void virtualNodes() {
        assertThat(ringOf("a", "b", "c").ringSize()).isEqualTo(3 * VIRTUAL_NODES);
    }

    @Test
    @DisplayName("keys past the last ring position wrap round to the first")
    void theRingWraps() {
        ConsistentHashRing ring = ringOf("only-node");

        // Whatever a key hashes to, there is exactly one node, so it must own everything.
        for (String key : keys(500)) {
            assertThat(ring.nodeFor(key)).isEqualTo("only-node");
        }
    }

    @Test
    @DisplayName("an empty ring has no owner, and says so")
    void emptyRing() {
        assertThatThrownBy(() -> new ConsistentHashRing(VIRTUAL_NODES).nodeFor("k"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("virtual nodes keep the distribution roughly even")
    void evenDistribution() {
        Map<String, Integer> distribution = ringOf("a", "b", "c", "d").keyDistribution(keys(10_000));

        System.out.println("  distribution over 4 nodes: " + distribution);

        assertThat(distribution).hasSize(4);
        assertThat(distribution.values())
                .as("2,500 each is perfect; virtual nodes should keep everyone within ~30%%")
                .allSatisfy(count -> assertThat(count).isBetween(1_750, 3_250));
    }

    @Test
    @DisplayName("THE test: adding a node moves about 1/N of keys, not most of them")
    void addingANodeMovesFewKeys() {
        List<String> keys = keys(10_000);

        ConsistentHashRing before = ringOf("a", "b", "c", "d");
        ConsistentHashRing after = ringOf("a", "b", "c", "d", "e");

        double consistent = ConsistentHashRing.keyMovementFraction(before, after, keys);
        double modulo = ModuloSharding.keyMovementFraction(4, 5, keys);

        System.out.printf("%n  going from 4 nodes to 5, over %,d keys:%n", keys.size());
        System.out.printf("    modulo hashing    : %.1f%% of keys move%n", modulo * 100);
        System.out.printf("    consistent hashing: %.1f%% of keys move%n%n", consistent * 100);

        assertThat(modulo)
                .as("modulo reshuffles nearly everything - this is the cold-start outage")
                .isGreaterThan(0.7);
        assertThat(consistent)
                .as("""
                        Only the keys in the new node's arc should move - about 1/5 of them.
                        If this is high, check that nodeFor walks CLOCKWISE from the key
                        (tailMap) rather than picking the nearest position in either direction.""")
                .isLessThan(0.30);
    }

    @Test
    @DisplayName("removing a node only redistributes that node's own keys")
    void removingANodeIsAlsoCheap() {
        List<String> keys = keys(10_000);

        ConsistentHashRing before = ringOf("a", "b", "c", "d");
        ConsistentHashRing after = ringOf("a", "b", "c", "d");
        after.removeNode("d");

        double moved = ConsistentHashRing.keyMovementFraction(before, after, keys);
        System.out.printf("  removing one node of four moved %.1f%% of keys%n", moved * 100);

        assertThat(moved)
                .as("only the departed node's share should move - roughly a quarter")
                .isLessThan(0.35);
    }

    @Test
    @DisplayName("keys that did not belong to the removed node stay exactly where they were")
    void survivorsDoNotMove() {
        ConsistentHashRing ring = ringOf("a", "b", "c");
        List<String> keys = keys(2_000);

        Map<String, String> ownersBefore = new java.util.HashMap<>();
        for (String key : keys) {
            ownersBefore.put(key, ring.nodeFor(key));
        }

        ring.removeNode("c");

        for (String key : keys) {
            if (!"c".equals(ownersBefore.get(key))) {
                assertThat(ring.nodeFor(key))
                        .as("key %s did not belong to the removed node", key)
                        .isEqualTo(ownersBefore.get(key));
            }
        }
    }

    @Test
    @DisplayName("more virtual nodes means a flatter distribution")
    void moreVirtualNodesFlattens() {
        List<String> keys = keys(10_000);

        ConsistentHashRing coarse = new ConsistentHashRing(1);
        ConsistentHashRing fine = new ConsistentHashRing(500);
        for (String node : List.of("a", "b", "c", "d", "e")) {
            coarse.addNode(node);
            fine.addNode(node);
        }

        int coarseSpread = spread(coarse.keyDistribution(keys));
        int fineSpread = spread(fine.keyDistribution(keys));

        System.out.printf("  spread with 1 virtual node: %,d   with 500: %,d%n",
                coarseSpread, fineSpread);

        assertThat(fineSpread)
                .as("this is exactly why virtual nodes exist")
                .isLessThan(coarseSpread);
    }

    private static int spread(Map<String, Integer> distribution) {
        return Collections.max(distribution.values()) - Collections.min(distribution.values());
    }

    @Test
    @DisplayName("a ring needs at least one virtual node per node")
    void rejectsNonsense() {
        assertThatThrownBy(() -> new ConsistentHashRing(0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
