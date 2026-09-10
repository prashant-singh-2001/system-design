package sd.p06.day54;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GIVEN, and correct-looking. This is the code almost everyone writes first.
 *
 * <p>Read the cache. On a miss, load and store. Thread-safe map, no visible race, no lost data.
 *
 * <p>And under concurrency it is a loaded gun. When a hot key expires, every in-flight request
 * misses at the same instant, and every one of them calls the source. One expiry becomes a
 * thousand simultaneous database queries. That is a <b>cache stampede</b> (also called a
 * thundering herd, or dog-piling), and it takes down the database precisely when traffic is
 * highest - which is exactly when the cache was supposed to be helping.
 *
 * <p>Note what makes it so nasty: nothing is wrong with this code in isolation. Every single
 * request behaves correctly. The failure is emergent, it only appears under load, and it
 * therefore never shows up in testing.
 */
public final class NaiveCache {

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final ExpensiveSource source;

    public NaiveCache(ExpensiveSource source) {
        this.source = source;
    }

    public String get(String key) {
        String cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        String loaded = source.load(key);       // every concurrent misser arrives here
        cache.put(key, loaded);
        return loaded;
    }
}
