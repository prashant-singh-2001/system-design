package sd.p05.day49;

/** Given a key and how many shards exist, decide which one owns it. */
public interface ShardingStrategy {

    int shardFor(String key, int shardCount);
}
