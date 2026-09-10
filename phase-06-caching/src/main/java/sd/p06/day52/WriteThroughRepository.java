package sd.p06.day52;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * TODO(day52): write-through. Every write goes to the cache AND the database, synchronously,
 * before the call returns.
 *
 * <p>Read: cache, then database on a miss (populate as you go).
 * Write: put in the cache and write the database, both before returning.
 *
 * <p>What it buys: the cache is never stale, and a just-written value is immediately readable
 * from cache - read-your-writes, for free.
 *
 * <p>What it costs: every write now pays cache latency plus database latency, and you cache data
 * that may never be read. On a write-heavy workload that is pure overhead.
 *
 * <p>And the honest caveat: two writes, two systems, no atomicity. If the database write fails
 * after the cache put, the cache is now lying. Order matters - write the database FIRST, so a
 * failure leaves the cache merely stale rather than confidently wrong.
 */
public final class WriteThroughRepository implements CachingRepository {

    private final Map<String, String> cache = new HashMap<>();
    private final SlowDatabase database;

    public WriteThroughRepository(SlowDatabase database) {
        this.database = database;
    }

    @Override
    public Optional<String> get(String key) {
        throw new UnsupportedOperationException("TODO(day52): cache, then database, then populate");
    }

    @Override
    public void put(String key, String value) {
        throw new UnsupportedOperationException("TODO(day52): database first, then cache");
    }

    @Override
    public String strategy() {
        return "write-through";
    }
}
