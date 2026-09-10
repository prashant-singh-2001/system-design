package sd.p05.day50;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Day50BenchmarkTest {

    private final ShardingBenchmark benchmark = new ShardingBenchmark();

    @Test
    @DisplayName("an unsalted, skewed workload produces a clearly measurable hot-shard load factor")
    void unsaltedWorkloadIsSkewed() {
        ShardedCounter store = new ShardedCounter(8);

        BenchmarkResult result = benchmark.runWorkload(store, 50_000, 0.9, 1);

        assertThat(result.requestsPerShard().values().stream().mapToLong(Long::longValue).sum())
                .as("every operation must be accounted for somewhere")
                .isEqualTo(50_000);
        assertThat(result.hotShardLoadFactor())
                .as("one shard absorbing 90%% of traffic should be several times the average load")
                .isGreaterThan(3.0);
    }

    @Test
    @DisplayName("salting the hot key measurably flattens the load factor, at the same scale")
    void saltedWorkloadIsFlatter() {
        ShardedCounter unsalted = new ShardedCounter(8);
        ShardedCounter salted = new ShardedCounter(8);

        BenchmarkResult unsaltedResult = benchmark.runWorkload(unsalted, 50_000, 0.9, 1);
        BenchmarkResult saltedResult = benchmark.runWorkload(salted, 50_000, 0.9, 8);

        assertThat(saltedResult.hotShardLoadFactor())
                .as("salting must produce a measurably flatter distribution than no salting at all")
                .isLessThan(unsaltedResult.hotShardLoadFactor() / 2);
        assertThat(saltedResult.hotShardLoadFactor()).isLessThan(2.0);
    }

    @Test
    @DisplayName("salting changes distribution, never the true total")
    void saltingPreservesCorrectness() {
        ShardedCounter store = new ShardedCounter(8);

        benchmark.runWorkload(store, 20_000, 0.9, 8);

        long expectedHotCount = (long) (20_000 * 0.9);
        assertThat(store.getSalted("hot-key", 8)).isEqualTo(expectedHotCount);
    }
}
