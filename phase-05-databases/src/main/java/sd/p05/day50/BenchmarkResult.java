package sd.p05.day50;

import java.time.Duration;
import java.util.Map;

/**
 * {@code hotShardLoadFactor} is the number that matters most: the busiest shard's request count
 * divided by the AVERAGE shard's request count. {@code 1.0} means perfectly even load; {@code
 * 4.0} means the busiest shard is handling four times its fair share.
 */
public record BenchmarkResult(Duration elapsed, Map<Integer, Long> requestsPerShard,
                               double hotShardLoadFactor) {
}
