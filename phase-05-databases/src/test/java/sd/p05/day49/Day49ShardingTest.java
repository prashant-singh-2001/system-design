package sd.p05.day49;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class Day49ShardingTest {

    @Test
    @DisplayName("HASH: the same key always lands on the same shard")
    void hashShardingIsDeterministic() {
        ShardingStrategy strategy = new HashShardingStrategy();

        int first = strategy.shardFor("user-42", 8);
        int second = strategy.shardFor("user-42", 8);

        assertThat(second).isEqualTo(first);
        assertThat(first).isBetween(0, 7);
    }

    @Test
    @DisplayName("RANGE: keys route to the shard owning their part of the key space")
    void rangeShardingRoutesByBoundary() {
        // Boundaries are full cut-point VALUES, not single letters - "mz" sorts after every
        // real word starting with "m" but before "n...", avoiding the prefix trap where a
        // short boundary like "m" would compare as LESS than a longer word such as "mango".
        ShardingStrategy strategy = new RangeShardingStrategy(List.of("fz", "mz", "tz"));

        assertThat(strategy.shardFor("apple", 4)).isEqualTo(0);
        assertThat(strategy.shardFor("grape", 4)).isEqualTo(1);
        assertThat(strategy.shardFor("mango", 4)).isEqualTo(1);
        assertThat(strategy.shardFor("orange", 4)).isEqualTo(2);
        assertThat(strategy.shardFor("zucchini", 4)).isEqualTo(3);
    }

    @Test
    @DisplayName("HOT KEY: a plain hashed counter concentrates a skewed workload on one shard")
    void hotKeyOverloadsOneShard() {
        ShardedCounterStore store = new ShardedCounterStore(8, new HashShardingStrategy());
        int totalOperations = 10_000;
        int hotOperations = (int) (totalOperations * 0.9);

        for (int i = 0; i < hotOperations; i++) {
            store.increment("viral-post");
        }
        for (int i = 0; i < totalOperations - hotOperations; i++) {
            store.increment("post-" + i);
        }

        Map<Integer, Long> perShard = store.requestCountPerShard();
        long maxShardLoad = perShard.values().stream().mapToLong(Long::longValue).max().orElseThrow();

        assertThat(maxShardLoad)
                .as("with 90%% of traffic on one key, one shard must absorb the overwhelming majority")
                .isGreaterThan(totalOperations / 2);
    }

    @Test
    @DisplayName("THE FIX: salting the hot key spreads its traffic across many shards")
    void saltingSpreadsHotKeyLoad() {
        ShardedCounterStore store = new ShardedCounterStore(8, new HashShardingStrategy());
        int hotOperations = 9_000;
        int saltFactor = 8;

        for (int i = 0; i < hotOperations; i++) {
            store.incrementSalted("viral-post", saltFactor);
        }

        Map<Integer, Long> perShard = store.requestCountPerShard();
        long maxShardLoad = perShard.values().stream().mapToLong(Long::longValue).max().orElseThrow();

        assertThat(maxShardLoad)
                .as("no single shard should still be absorbing anywhere near all the traffic")
                .isLessThan((long) (hotOperations * 0.5));
        assertThat(store.getSalted("viral-post", saltFactor))
                .as("the fix must not lose or double-count a single increment")
                .isEqualTo(hotOperations);
    }

    @Test
    @DisplayName("a sharded counter rejects a nonsensical shard count")
    void rejectsInvalidShardCount() {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> new ShardedCounterStore(0, new HashShardingStrategy()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
