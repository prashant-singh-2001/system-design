package sd.p05.day49;

/**
 * TODO(day49): the Day 21 formula again - {@code Math.floorMod(key.hashCode(), shardCount)}.
 * Deterministic, and spreads DISTINCT keys evenly across shards. What it does nothing about:
 * how often each key is actually ACCESSED - a key hashes to the same shard every time, no matter
 * how many times a second it is hit, which is precisely how one popular key can overload one
 * shard while every other shard sits idle.
 */
public final class HashShardingStrategy implements ShardingStrategy {

    @Override
    public int shardFor(String key, int shardCount) {
        throw new UnsupportedOperationException("TODO(day49): floorMod the key's hash by shardCount");
    }
}
