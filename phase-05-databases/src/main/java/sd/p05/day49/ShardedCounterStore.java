package sd.p05.day49;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TODO(day49): a counter per key, sharded, with both the HOT KEY problem and its standard fix
 * built side by side so you can measure the difference rather than take it on faith.
 *
 * <p>{@code increment(key)} / {@code get(key)}: route through {@code strategy.shardFor(key,
 * shardCount)} to find which shard's map to touch, then increment or read a plain counter
 * there. Every call for the SAME key always lands on the SAME shard - which is exactly the
 * problem: a viral key gets hit thousands of times a second, and every single one of those hits
 * lands on one shard, no matter how many shards you have.
 *
 * <p>{@code requestCountPerShard()}: not "how many distinct keys live on each shard" - how many
 * OPERATIONS (increments, specifically) have actually been ROUTED to each shard. This is the
 * number that makes a hot shard visible: key COUNT can look perfectly balanced while request
 * TRAFFIC to one shard is wildly disproportionate.
 *
 * <p><b>The fix - key salting.</b> {@code incrementSalted(key, saltFactor)}: pick a random
 * integer in {@code [0, saltFactor)} and increment {@code key + "#" + salt} instead of
 * {@code key} directly - spreading what would have been one hot key's traffic across up to
 * {@code saltFactor} different keys, which (via the same {@code strategy}) land on up to
 * {@code saltFactor} different shards. {@code getSalted(key, saltFactor)}: sum
 * {@code get(key + "#" + i)} for every {@code i} from {@code 0} to {@code saltFactor - 1} - the
 * read now has to FAN OUT across every salted variant and add them back together, which is
 * exactly the cost this fix trades for spreading the write load.
 */
public final class ShardedCounterStore {

    private final ShardingStrategy strategy;
    private final int shardCount;
    private final List<Map<String, Long>> shardData;
    private final List<AtomicLong> shardRequestCounts;

    public ShardedCounterStore(int shardCount, ShardingStrategy strategy) {
        throw new UnsupportedOperationException(
                "TODO(day49): validate shardCount >= 1, init one map and one counter per shard");
    }

    public void increment(String key) {
        throw new UnsupportedOperationException(
                "TODO(day49): route to a shard via the strategy, bump its counter for this key");
    }

    public long get(String key) {
        throw new UnsupportedOperationException("TODO(day49): read this key's counter from its shard");
    }

    public void incrementSalted(String key, int saltFactor) {
        throw new UnsupportedOperationException(
                "TODO(day49): increment(key + \"#\" + random salt in [0, saltFactor))");
    }

    public long getSalted(String key, int saltFactor) {
        throw new UnsupportedOperationException(
                "TODO(day49): sum get(key + \"#\" + i) for i in [0, saltFactor)");
    }

    public Map<Integer, Long> requestCountPerShard() {
        throw new UnsupportedOperationException(
                "TODO(day49): shardIndex -> total increment() calls routed there");
    }
}
