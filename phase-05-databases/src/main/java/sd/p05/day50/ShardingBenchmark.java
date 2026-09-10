package sd.p05.day50;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * TODO(day50) - the phase review's "Build": run a realistic-sized skewed workload through
 * Day 49's {@link ShardedCounter}, with and without salting, and turn "one shard seems busier"
 * into an actual NUMBER you can put in a write-up.
 *
 * <p>{@code runWorkload(store, operationCount, hotKeyFraction, saltFactor)}:
 * <ol>
 *   <li>let {@code hotCount = (int) (operationCount * hotKeyFraction)}</li>
 *   <li>time the whole run: record {@code Instant.now()} before and after</li>
 *   <li>for {@code hotCount} operations: if {@code saltFactor > 1}, call
 *       {@code store.incrementSalted("hot-key", saltFactor)}; otherwise call
 *       {@code store.increment("hot-key")}</li>
 *   <li>for the remaining {@code operationCount - hotCount} operations: call
 *       {@code store.increment("key-" + i)} for a distinct {@code i} each time, spreading them
 *       across many different keys</li>
 *   <li>read {@code store.requestCountPerShard()}, and compute {@code hotShardLoadFactor} as
 *       the maximum shard's count divided by the AVERAGE shard count across all shards</li>
 *   <li>return a {@link BenchmarkResult} with the elapsed {@link Duration}, the per-shard
 *       counts, and the load factor</li>
 * </ol>
 */
public final class ShardingBenchmark {

    public BenchmarkResult runWorkload(ShardedCounter store, int operationCount,
                                        double hotKeyFraction, int saltFactor) {
        throw new UnsupportedOperationException(
                "TODO(day50): run the skewed workload, time it, compute the hot-shard load factor");
    }
}
