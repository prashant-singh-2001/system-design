package sd.p06.day52;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * TODO(day52): cache-aside (lazy loading). By far the most common strategy in production.
 *
 * <p>Read: look in the cache; on a miss, read the database and populate the cache.
 * Write: write the database, then <b>invalidate</b> the cache entry.
 *
 * <p>Why invalidate rather than update? Because updating means the cache holds a value that a
 * concurrent writer may already have superseded, and you have no ordering guarantee between two
 * clients racing to set it. Deleting is idempotent and always safe: the worst outcome is an
 * extra miss. "Invalidate, do not update" is the single most useful rule in caching.
 *
 * <p>Cost: every miss costs the full database latency, and the application owns all the cache
 * logic. Benefit: only requested data is ever cached, and a cache outage degrades to slow rather
 * than broken.
 */
public final class CacheAsideRepository implements CachingRepository {

    private final Map<String, String> cache = new HashMap<>();
    private final SlowDatabase database;

    public CacheAsideRepository(SlowDatabase database) {
        this.database = database;
    }

    @Override
    public Optional<String> get(String key) {
        throw new UnsupportedOperationException("TODO(day52): cache first, then database, then populate");
    }

    @Override
    public void put(String key, String value) {
        throw new UnsupportedOperationException("TODO(day52): write the database, then invalidate");
    }

    @Override
    public String strategy() {
        return "cache-aside";
    }
}
