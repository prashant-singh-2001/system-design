package sd.p06.day52;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * TODO(day52): write-behind (write-back). Writes land in the cache and return immediately;
 * the database is updated later, in a batch.
 *
 * <p>Read: cache, then database on a miss.
 * Write: put in the cache, record it as pending, and return. Do NOT touch the database.
 * {@link #flush()}: write every pending entry to the database, then clear the pending set.
 *
 * <p>This is the fastest possible write and the most dangerous. It is exactly the strategy your
 * CPU uses for its write-back cache, and exactly the strategy an LSM memtable uses (Day 47) -
 * absorb writes in fast memory, persist in batches, because batching beats chatter.
 *
 * <p>The cost is stated plainly: <b>an unflushed write is lost if the process dies.</b> You have
 * traded durability for throughput. That is sometimes exactly right - view counters, metrics,
 * last-seen timestamps - and catastrophic for anything you would not casually lose.
 *
 * <p>Note that Day 48's write-ahead log is the answer to this exact problem, if you need both.
 * Batch to the database, but log durably first.
 *
 * <p>Keep the pending map insertion-ordered so a flush is deterministic and testable.
 */
public final class WriteBehindRepository implements CachingRepository {

    private final Map<String, String> cache = new HashMap<>();
    private final Map<String, String> pending = new LinkedHashMap<>();
    private final SlowDatabase database;

    public WriteBehindRepository(SlowDatabase database) {
        this.database = database;
    }

    @Override
    public Optional<String> get(String key) {
        throw new UnsupportedOperationException("TODO(day52): cache, then database, then populate");
    }

    @Override
    public void put(String key, String value) {
        throw new UnsupportedOperationException("TODO(day52): cache it, mark it pending, return");
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException("TODO(day52): drain pending to the database");
    }

    /** How many writes are sitting in memory, unpersisted. This number is your risk window. */
    public int pendingWrites() {
        return pending.size();
    }

    @Override
    public String strategy() {
        return "write-behind";
    }
}
