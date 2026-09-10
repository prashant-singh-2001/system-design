package sd.p09.day89;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sd.p09.capstone.adapter.CachingLinkRepository;
import sd.p09.capstone.adapter.ResilientLinkRepository;
import sd.p09.capstone.adapter.SequentialCodeGenerator;
import sd.p09.capstone.domain.LinkService;
import sd.p09.capstone.domain.ShortLink;
import sd.p09.support.MutableClock;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day89WiringTest {

    private MutableClock clock;
    private FailingLinkRepository database;

    @BeforeEach
    void setUp() {
        clock = MutableClock.startingAt("2026-03-01T12:00:00Z");
        database = new FailingLinkRepository();
    }

    private LinkService serviceWithCache(CachingLinkRepository cache) {
        return new LinkService(cache, new SequentialCodeGenerator(), clock);
    }

    @Test
    @DisplayName("the cache collapses repeated redirects to one database read")
    void cacheAbsorbsTheHotPath() {
        CachingLinkRepository cache = new CachingLinkRepository(database);
        LinkService service = serviceWithCache(cache);

        ShortLink link = service.shorten("https://example.com/viral");
        database.resetCalls();

        for (int i = 0; i < 100; i++) {
            assertThat(service.resolve(link.code())).contains("https://example.com/viral");
        }

        System.out.printf("  100 redirects -> %d database calls, hit ratio %.1f%%%n",
                database.calls(), cache.hitRatio() * 100);

        assertThat(database.calls())
                .as("one miss to populate, then ninety-nine hits")
                .isEqualTo(1);
        assertThat(cache.hitRatio()).isGreaterThan(0.98);
    }

    @Test
    @DisplayName("a write invalidates rather than updating - Day 52's rule, enforced")
    void writesInvalidate() {
        CachingLinkRepository cache = new CachingLinkRepository(database);
        LinkService service = serviceWithCache(cache);

        ShortLink link = service.shorten("https://example.com/counted");
        service.resolve(link.code());
        service.recordClick(link.code());
        database.resetCalls();

        service.resolve(link.code());

        assertThat(database.calls())
                .as("""
                        The click changed the row, so the cached copy is stale and must go. Updating
                        it in place instead would race with any concurrent writer - deleting is
                        idempotent and order-independent.""")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("a missing code is not cached as a hit")
    void missesAreNotCachedAccidentally() {
        CachingLinkRepository cache = new CachingLinkRepository(database);

        cache.findByCode("nope");
        cache.findByCode("nope");

        assertThat(cache.hits())
                .as("caching a negative result must be a deliberate decision, not an accident")
                .isZero();
    }

    @Test
    @DisplayName("the breaker trips and reads degrade to empty instead of hanging")
    void breakerDegradesReads() {
        ResilientLinkRepository resilient =
                new ResilientLinkRepository(database, 3, Duration.ofSeconds(30), clock);

        database.goDown();

        for (int i = 0; i < 3; i++) {
            resilient.findByCode("abc");
        }
        assertThat(resilient.isOpen()).isTrue();

        database.resetCalls();
        for (int i = 0; i < 50; i++) {
            assertThat(resilient.findByCode("abc")).isEmpty();
        }

        assertThat(database.calls())
                .as("""
                        Fifty requests, zero database calls. The caller gets a 404 in microseconds
                        rather than a thread parked on a timeout - which is what stops a database
                        outage from becoming a total outage.""")
                .isZero();
        assertThat(resilient.shortCircuited()).isEqualTo(50);
    }

    @Test
    @DisplayName("writes fail loudly when the breaker is open - silence would be worse")
    void breakerFailsWritesLoudly() {
        ResilientLinkRepository resilient =
                new ResilientLinkRepository(database, 3, Duration.ofSeconds(30), clock);
        database.goDown();
        for (int i = 0; i < 3; i++) {
            resilient.findByCode("abc");
        }

        assertThatThrownBy(() -> resilient.save(
                new ShortLink("x", "https://example.com", clock.instant(), 0)))
                .as("silently discarding a write is worse than failing it")
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("the breaker recovers on a successful probe")
    void breakerRecovers() {
        ResilientLinkRepository resilient =
                new ResilientLinkRepository(database, 3, Duration.ofSeconds(30), clock);
        database.goDown();
        for (int i = 0; i < 3; i++) {
            resilient.findByCode("abc");
        }

        database.recover();
        clock.advance(Duration.ofSeconds(31));

        resilient.findByCode("abc");

        assertThat(resilient.isOpen()).isFalse();
    }

    @Test
    @DisplayName("THE wiring lesson: cache OUTSIDE the breaker keeps redirects alive in an outage")
    void cacheOutsideBreakerDegradesGracefully() {
        ResilientLinkRepository resilient =
                new ResilientLinkRepository(database, 3, Duration.ofSeconds(30), clock);
        CachingLinkRepository cache = new CachingLinkRepository(resilient);
        LinkService service = new LinkService(cache, new SequentialCodeGenerator(), clock);

        ShortLink link = service.shorten("https://example.com/important");
        service.resolve(link.code());          // warms the cache

        database.goDown();

        assertThat(service.resolve(link.code()))
                .as("""
                        The database is completely down and the redirect still works, because the
                        cache sits outside the breaker and never asks. Reverse the two and every
                        redirect fails. That is graceful degradation decided by one line of
                        wiring - and the most valuable thing this capstone demonstrates.""")
                .contains("https://example.com/important");
    }
}
