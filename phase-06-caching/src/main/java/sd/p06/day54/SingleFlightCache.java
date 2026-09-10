package sd.p06.day54;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO(day54): fix the stampede with SINGLE-FLIGHT - one loader per key, everyone else waits.
 *
 * <p>The realisation that makes this easy: {@code ConcurrentHashMap.computeIfAbsent} already
 * does exactly this. It holds a lock on the key's bin while the mapping function runs, so
 * concurrent callers for the SAME key block until the first one finishes, then read its result.
 * Different keys hash to different bins and proceed in parallel, which is what you want.
 *
 * <p>So the whole fix is one line. That is worth sitting with: the bug is subtle and the remedy
 * is trivial, which is precisely why it keeps being shipped.
 *
 * <p>The trade-off you accept: concurrent readers of a cold key now wait for one load rather
 * than each doing their own. Their latency is the same as the naive version - they were going
 * to wait for a load anyway - but the database sees one query instead of N.
 *
 * <p>The caveat worth knowing: a slow load now blocks every waiter on that key, so a genuinely
 * stuck loader converts a stampede into a pile-up. In production you pair this with a timeout.
 */
public final class SingleFlightCache {

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final ExpensiveSource source;

    public SingleFlightCache(ExpensiveSource source) {
        this.source = source;
    }

    public String get(String key) {
        throw new UnsupportedOperationException("TODO(day54): one loader per key");
    }
}
