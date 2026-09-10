package sd.p09.capstone.adapter;

import sd.p09.capstone.domain.LinkRepository;
import sd.p09.capstone.domain.ShortLink;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TODO(day89): cache-aside as a DECORATOR around any repository.
 *
 * <p>Two ideas from earlier phases meeting: Day 24's Decorator and Day 52's cache-aside. Because
 * this implements the same port it wraps, the domain cannot tell the difference and nothing above
 * it changes. That is the payoff of having built the seam on Day 88.
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code findByCode} - cache first (count a hit), else delegate and populate (count a miss).
 *       A miss that finds nothing must NOT be cached as a hit - or, if you do cache negatives, be
 *       deliberate about it, because that is how you defend against a scan for random codes.</li>
 *   <li>{@code save} - write through the delegate, then <b>invalidate</b> the entry. Day 52's rule:
 *       invalidate, do not update. Deleting is idempotent and order-independent; updating races.</li>
 *   <li>{@code incrementClicks} - delegate, then invalidate, because the cached copy now has a
 *       stale click count.</li>
 *   <li>{@code findByTargetUrl} - delegate without caching. It is the write path, not the hot
 *       path, and caching it would need a second key space for very little benefit.</li>
 * </ul>
 *
 * <p>Note what you are buying and what it costs. The redirect path is 50:1 read-heavy, so a cache
 * collapses p50 - and, as Day 60 measured, barely moves p99, because misses still pay full price.
 */
public final class CachingLinkRepository implements LinkRepository {

    private final LinkRepository delegate;
    private final Map<String, ShortLink> cache = new HashMap<>();
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();

    public CachingLinkRepository(LinkRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public void save(ShortLink link) {
        throw new UnsupportedOperationException("TODO(day89): write through, then invalidate");
    }

    @Override
    public Optional<ShortLink> findByCode(String code) {
        throw new UnsupportedOperationException("TODO(day89): cache-aside on the hot path");
    }

    @Override
    public Optional<ShortLink> findByTargetUrl(String targetUrl) {
        throw new UnsupportedOperationException("TODO(day89): delegate; this is the write path");
    }

    @Override
    public Optional<Long> incrementClicks(String code) {
        throw new UnsupportedOperationException("TODO(day89): delegate, then invalidate");
    }

    public long hits() {
        return hits.get();
    }

    public long misses() {
        return misses.get();
    }

    public double hitRatio() {
        long total = hits.get() + misses.get();
        return total == 0 ? 0 : (double) hits.get() / total;
    }
}
