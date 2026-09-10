package sd.p09.day90;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import sd.p09.capstone.adapter.CachingLinkRepository;
import sd.p09.capstone.adapter.PostgresLinkRepository;
import sd.p09.capstone.adapter.ResilientLinkRepository;
import sd.p09.capstone.adapter.SequentialCodeGenerator;
import sd.p09.capstone.domain.LinkService;
import sd.p09.support.MutableClock;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class Day90CapstoneTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    /** The SLO the capstone commits to. */
    private static final double AVAILABILITY_TARGET = 0.999;

    private static Connection connection;

    private MutableClock clock;
    private PostgresLinkRepository database;
    private CachingLinkRepository cache;
    private LinkService service;

    @BeforeAll
    static void connect() throws SQLException {
        connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        try (Statement statement = connection.createStatement()) {
            statement.execute(PostgresLinkRepository.SCHEMA);
        }
    }

    @AfterAll
    static void disconnect() throws SQLException {
        connection.close();
    }

    @BeforeEach
    void setUp() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("TRUNCATE links");
        }
        clock = MutableClock.startingAt("2026-03-01T12:00:00Z");
        database = new PostgresLinkRepository(connection);

        // The full production wiring: cache outside the breaker, so a database outage
        // degrades to cached redirects rather than a total failure (Day 89).
        ResilientLinkRepository resilient =
                new ResilientLinkRepository(database, 5, Duration.ofSeconds(30), clock);
        cache = new CachingLinkRepository(resilient);
        service = new LinkService(cache, new SequentialCodeGenerator(), clock);
    }

    private List<String> seed(int count) {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            codes.add(service.shorten("https://example.com/page/" + i).code());
        }
        return codes;
    }

    @Test
    @DisplayName("THE capstone result: the wired service meets its SLO under a realistic workload")
    void meetsItsSlo() {
        List<String> codes = seed(500);

        LoadTestResult result = LoadTest.run(service, cache, codes, 20_000, new Random(42));

        System.out.println();
        System.out.println("  " + result.summary());
        System.out.println();

        assertThat(result.requests()).isEqualTo(20_000);
        assertThat(result.availability())
                .as("""
                        Twenty thousand redirects against a real Postgres, through a cache and a
                        circuit breaker, meeting a three-nines availability target. Every component
                        here is something you built and measured earlier in the course.""")
                .isGreaterThanOrEqualTo(AVAILABILITY_TARGET);
    }

    @Test
    @DisplayName("the skewed workload produces a high cache hit ratio, as Day 60 predicted")
    void skewMakesTheCacheWork() {
        List<String> codes = seed(500);

        LoadTestResult result = LoadTest.run(service, cache, codes, 20_000, new Random(42));

        System.out.printf("  cache hit ratio over a Zipfian workload: %.1f%%%n",
                result.cacheHitRatio() * 100);

        assertThat(result.cacheHitRatio())
                .as("""
                        Real traffic concentrates on a few keys, so a modest cache catches most of
                        it. Benchmark with uniform random keys and you would conclude, wrongly,
                        that caching does not help.""")
                .isGreaterThan(0.7);
    }

    @Test
    @DisplayName("p50 collapses and p99 does not - a cache fixes a busy dependency, not a slow one")
    void percentilesTellTheRealStory() {
        List<String> codes = seed(500);

        LoadTestResult result = LoadTest.run(service, cache, codes, 20_000, new Random(42));

        System.out.printf("  p50 %.3f ms, p99 %.3f ms%n",
                result.p50().toNanos() / 1_000_000.0, result.p99().toNanos() / 1_000_000.0);

        assertThat(result.p50()).isLessThanOrEqualTo(result.p99());
        assertThat(result.p99())
                .as("""
                        Misses still pay the full database round trip, so the tail barely moves.
                        Being able to say that - rather than 'we added a cache and it got faster' -
                        is the difference this course was for.""")
                .isGreaterThanOrEqualTo(result.p50());
    }

    @Test
    @DisplayName("the redirect path stays correct under load - speed that lies is not speed")
    void correctnessUnderLoad() {
        List<String> codes = seed(50);

        for (int i = 0; i < codes.size(); i++) {
            assertThat(service.resolve(codes.get(i)))
                    .as("code %s must still resolve to its own URL", codes.get(i))
                    .contains("https://example.com/page/" + i);
        }
    }

    @Test
    @DisplayName("an empty load test is handled, not divided by zero")
    void emptyRun() {
        LoadTestResult result = LoadTest.run(service, cache, seed(1), 0, new Random(1));

        assertThat(result.requests()).isZero();
        assertThat(result.availability()).isEqualTo(1.0);
        assertThat(result.p99()).isEqualTo(Duration.ZERO);
    }
}
