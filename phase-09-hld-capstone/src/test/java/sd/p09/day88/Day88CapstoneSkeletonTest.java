package sd.p09.day88;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import sd.p09.capstone.adapter.PostgresLinkRepository;
import sd.p09.capstone.adapter.SequentialCodeGenerator;
import sd.p09.capstone.domain.LinkService;
import sd.p09.capstone.domain.ShortLink;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class Day88CapstoneSkeletonTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    private static final Instant FIXED = Instant.parse("2026-03-01T12:00:00Z");
    private static Connection connection;

    private PostgresLinkRepository repository;
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
        repository = new PostgresLinkRepository(connection);
        service = new LinkService(repository, new SequentialCodeGenerator(),
                Clock.fixed(FIXED, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("shorten then resolve, against a real database")
    void roundTrip() {
        ShortLink link = service.shorten("https://example.com/a/long/path");

        assertThat(link.code()).isNotBlank();
        assertThat(link.createdAt()).as("the injected clock is used").isEqualTo(FIXED);
        assertThat(service.resolve(link.code())).contains("https://example.com/a/long/path");
    }

    @Test
    @DisplayName("shortening the same URL twice returns the same link")
    void idempotent() {
        ShortLink first = service.shorten("https://example.com/same");
        ShortLink second = service.shorten("https://example.com/same");

        assertThat(second.code())
                .as("a business rule in the domain, and a UNIQUE constraint in the schema")
                .isEqualTo(first.code());
    }

    @Test
    @DisplayName("the URL rule lives in the domain")
    void rejectsBadUrls() {
        assertThatThrownBy(() -> service.shorten("ftp://example.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.shorten("javascript:alert(1)"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("an unknown code resolves to empty")
    void unknownCode() {
        assertThat(service.resolve("nope")).isEmpty();
        assertThat(service.recordClick("nope")).isEmpty();
    }

    @Test
    @DisplayName("clicks increment atomically in the database, never read-modify-write")
    void clicksIncrement() {
        ShortLink link = service.shorten("https://example.com/tracked");

        assertThat(service.recordClick(link.code())).contains(1L);
        assertThat(service.recordClick(link.code())).contains(2L);
        assertThat(service.recordClick(link.code())).contains(3L);

        assertThat(repository.findByCode(link.code()))
                .hasValueSatisfying(stored -> assertThat(stored.clicks()).isEqualTo(3));
    }

    @Test
    @DisplayName("the redirect path is one query - it is the hot path")
    void resolveIsOneQuery() {
        ShortLink link = service.shorten("https://example.com/hot");
        repository.resetQueryCount();

        service.resolve(link.code());

        assertThat(repository.queries())
                .as("a single index probe on the primary key")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("ARCHITECTURE: the domain references no adapter")
    void domainIsIndependent() throws IOException {
        Path domain = Path.of("src/main/java/sd/p09/capstone/domain");

        try (Stream<Path> sources = Files.walk(domain)) {
            List<String> offenders = sources
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> {
                        try {
                            return Files.readString(p).contains("sd.p09.capstone.adapter");
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .map(p -> p.getFileName().toString())
                    .toList();

            assertThat(offenders)
                    .as("""
                            The same fitness function as Day 17, now guarding a service that talks
                            to a real database. Swapping Postgres for anything else must not touch
                            a line of business logic.""")
                    .isEmpty();
        }
    }
}
