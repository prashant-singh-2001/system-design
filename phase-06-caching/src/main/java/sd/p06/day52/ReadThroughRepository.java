package sd.p06.day52;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * TODO(day52): read-through. Identical outcome to cache-aside on reads, different OWNERSHIP.
 *
 * <p>The caller only ever talks to the cache. The cache itself knows how to fetch a missing
 * value - here, via {@code computeIfAbsent} over the database.
 *
 * <p>The difference is not performance, it is where the logic lives. Cache-aside puts it in
 * every caller; read-through puts it in one place. That matters when six services share a cache
 * and you want one definition of "how do we load a user".
 *
 * <p>Writes here behave like cache-aside: write the database, invalidate the entry.
 */
public final class ReadThroughRepository implements CachingRepository {

    private final Map<String, String> cache = new HashMap<>();
    private final SlowDatabase database;

    public ReadThroughRepository(SlowDatabase database) {
        this.database = database;
    }

    @Override
    public Optional<String> get(String key) {
        throw new UnsupportedOperationException("TODO(day52): the cache loads its own misses");
    }

    @Override
    public void put(String key, String value) {
        throw new UnsupportedOperationException("TODO(day52): write the database, then invalidate");
    }

    @Override
    public String strategy() {
        return "read-through";
    }
}
