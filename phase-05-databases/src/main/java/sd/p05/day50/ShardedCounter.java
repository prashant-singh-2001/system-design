package sd.p05.day50;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * GIVEN - Day 49's sharded counter, working, so today's TODO can focus entirely on measuring it
 * at a larger scale rather than re-deriving mechanics you already built once.
 */
public final class ShardedCounter {

    private final int shardCount;
    private final List<Map<String, Long>> shardData = new ArrayList<>();
    private final List<AtomicLong> shardRequestCounts = new ArrayList<>();

    public ShardedCounter(int shardCount) {
        this.shardCount = shardCount;
        for (int i = 0; i < shardCount; i++) {
            shardData.add(new HashMap<>());
            shardRequestCounts.add(new AtomicLong(0));
        }
    }

    private int shardFor(String key) {
        return Math.floorMod(key.hashCode(), shardCount);
    }

    public void increment(String key) {
        int shard = shardFor(key);
        shardData.get(shard).merge(key, 1L, Long::sum);
        shardRequestCounts.get(shard).incrementAndGet();
    }

    public void incrementSalted(String key, int saltFactor) {
        int salt = ThreadLocalRandom.current().nextInt(saltFactor);
        increment(key + "#" + salt);
    }

    public long get(String key) {
        return shardData.get(shardFor(key)).getOrDefault(key, 0L);
    }

    public long getSalted(String key, int saltFactor) {
        long total = 0;
        for (int i = 0; i < saltFactor; i++) {
            total += get(key + "#" + i);
        }
        return total;
    }

    public Map<Integer, Long> requestCountPerShard() {
        Map<Integer, Long> result = new LinkedHashMap<>();
        for (int i = 0; i < shardCount; i++) {
            result.put(i, shardRequestCounts.get(i).get());
        }
        return result;
    }
}
