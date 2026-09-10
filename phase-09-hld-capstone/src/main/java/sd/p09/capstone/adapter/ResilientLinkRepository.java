package sd.p09.capstone.adapter;

import sd.p09.capstone.domain.LinkRepository;
import sd.p09.capstone.domain.ShortLink;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TODO(day89): a circuit breaker as a DECORATOR, so the domain never learns the database can fail.
 *
 * <p>Day 73's state machine, applied at a port boundary. Wrapping order matters and repeats Day
 * 80's argument:
 *
 * <pre>
 *   ResilientLinkRepository( CachingLinkRepository( PostgresLinkRepository ) )
 * </pre>
 *
 * <p>The breaker outermost means a tripped breaker skips the cache lookup too - which is the wrong
 * choice here, and worth arguing about in your design document. Put the cache outermost instead
 * and a cached redirect keeps working through a total database outage. **That is graceful
 * degradation in one line of wiring**, and it is the single most valuable thing this capstone
 * demonstrates.
 *
 * <p>Implement the breaker: count consecutive failures, open at {@code failureThreshold}, and while
 * open return {@code Optional.empty()} for reads rather than throwing - the caller sees "not
 * found" and can serve a 404 in microseconds instead of hanging. Writes while open throw, because
 * silently discarding a write would be worse than failing it.
 *
 * <p>After {@code openDuration}, allow one probe. Success closes; failure reopens.
 */
public final class ResilientLinkRepository implements LinkRepository {

    private final LinkRepository delegate;
    private final int failureThreshold;
    private final Duration openDuration;
    private final Clock clock;

    private int consecutiveFailures;
    private Instant openedAt;
    private final AtomicLong shortCircuited = new AtomicLong();

    public ResilientLinkRepository(LinkRepository delegate, int failureThreshold,
                                   Duration openDuration, Clock clock) {
        this.delegate = delegate;
        this.failureThreshold = failureThreshold;
        this.openDuration = openDuration;
        this.clock = clock;
    }

    @Override
    public void save(ShortLink link) {
        throw new UnsupportedOperationException("TODO(day89): writes fail loudly when open");
    }

    @Override
    public Optional<ShortLink> findByCode(String code) {
        throw new UnsupportedOperationException("TODO(day89): reads degrade to empty when open");
    }

    @Override
    public Optional<ShortLink> findByTargetUrl(String targetUrl) {
        throw new UnsupportedOperationException("TODO(day89): same read policy");
    }

    @Override
    public Optional<Long> incrementClicks(String code) {
        throw new UnsupportedOperationException("TODO(day89): analytics may be dropped when open");
    }

    public boolean isOpen() {
        throw new UnsupportedOperationException("TODO(day89): report breaker state");
    }

    /** Calls that never reached the database because the breaker was open. */
    public long shortCircuited() {
        return shortCircuited.get();
    }
}
