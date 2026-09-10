package sd.p03.day21;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day21PartitioningTest {

    @Test
    @DisplayName("FNV-1a matches published reference values")
    void fnv1aKnownVectors() {
        Fnv1aHashFunction fnv1a = new Fnv1aHashFunction();

        assertThat(fnv1a.hash("")).isEqualTo(2_166_136_261L);
        assertThat(fnv1a.hash("a")).isEqualTo(3_826_002_220L);
        assertThat(fnv1a.hash("foobar")).isEqualTo(3_214_735_720L);
    }

    @Test
    @DisplayName("every hash function is deterministic across repeated calls")
    void deterministic() {
        for (HashFunction fn : new HashFunction[]{new JavaHashFunction(), new Fnv1aHashFunction()}) {
            long first = fn.hash("order-42");
            long second = fn.hash("order-42");
            assertThat(second).as(fn.getClass().getSimpleName()).isEqualTo(first);
        }
    }

    @Test
    @DisplayName("the factory dispatches by name")
    void factoryDispatchesByName() {
        assertThat(HashFunctions.byName("java")).isInstanceOf(JavaHashFunction.class);
        assertThat(HashFunctions.byName("fnv1a")).isInstanceOf(Fnv1aHashFunction.class);
    }

    @Test
    @DisplayName("an unrecognised name fails loudly rather than silently defaulting")
    void factoryRejectsUnknownName() {
        assertThatThrownBy(() -> HashFunctions.byName("murmur3"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("murmur3");
    }

    @Test
    @DisplayName("a partitioner needs at least one partition")
    void rejectsZeroPartitions() {
        assertThatThrownBy(() -> new Partitioner(new Fnv1aHashFunction(), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("the same key always lands in the same partition")
    void partitioningIsDeterministic() {
        Partitioner partitioner = new Partitioner(new Fnv1aHashFunction(), 8);

        int first = partitioner.partitionFor("user-42");
        int second = partitioner.partitionFor("user-42");

        assertThat(second).isEqualTo(first);
        assertThat(first).isBetween(0, 7);
    }

    @Test
    @DisplayName("keys spread across every partition, not just a few")
    void keysSpreadAcrossAllPartitions() {
        Partitioner partitioner = new Partitioner(new Fnv1aHashFunction(), 8);

        Set<Integer> touched = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            touched.add(partitioner.partitionFor("key-" + i));
        }

        assertThat(touched)
                .as("500 keys over 8 partitions should exercise every one of them")
                .containsExactlyInAnyOrder(0, 1, 2, 3, 4, 5, 6, 7);
    }

    @Test
    @DisplayName("THE reason Day 56 exists: resizing moves most keys to a new partition")
    void resizingMovesMostKeys() {
        HashFunction hashFunction = new Fnv1aHashFunction();
        Partitioner fourPartitions = new Partitioner(hashFunction, 4);
        Partitioner fivePartitions = new Partitioner(hashFunction, 5);

        int moved = 0;
        int total = 200;
        for (int i = 0; i < total; i++) {
            String key = "key-" + i;
            if (fourPartitions.partitionFor(key) != fivePartitions.partitionFor(key)) {
                moved++;
            }
        }

        assertThat(moved)
                .as("modulo hashing has no way to add capacity gracefully - almost every key moves")
                .isGreaterThan(total / 2);
    }
}
