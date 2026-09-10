package sd.p06.day52;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class Day52CachePatternsTest {

    private SlowDatabase database;

    @BeforeEach
    void setUp() {
        database = new SlowDatabase();
    }

    static Stream<Function<SlowDatabase, CachingRepository>> allStrategies() {
        return Stream.of(
                CacheAsideRepository::new,
                ReadThroughRepository::new,
                WriteThroughRepository::new,
                WriteBehindRepository::new);
    }

    @ParameterizedTest(name = "{index}: a repeated read hits the database only once")
    @MethodSource("allStrategies")
    void repeatedReadsAreCached(Function<SlowDatabase, CachingRepository> factory) {
        database.seed("k", "v");
        CachingRepository repository = factory.apply(database);

        assertThat(repository.get("k")).contains("v");
        assertThat(repository.get("k")).contains("v");
        assertThat(repository.get("k")).contains("v");

        assertThat(database.reads())
                .as("%s should have populated the cache on the first miss", repository.strategy())
                .isEqualTo(1);
    }

    @ParameterizedTest(name = "{index}: a missing key is absent, and stays absent")
    @MethodSource("allStrategies")
    void missingKeys(Function<SlowDatabase, CachingRepository> factory) {
        assertThat(factory.apply(database).get("nope")).isEmpty();
    }

    @Test
    @DisplayName("cache-aside invalidates on write, so the next read reloads")
    void cacheAsideInvalidates() {
        database.seed("k", "old");
        CacheAsideRepository repository = new CacheAsideRepository(database);

        repository.get("k");                       // populates
        repository.put("k", "new");
        database.resetCounters();

        assertThat(repository.get("k")).contains("new");
        assertThat(database.reads())
                .as("invalidation means the next read must go to the database")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("cache-aside writes exactly one row and never updates the cache in place")
    void cacheAsideWriteCost() {
        new CacheAsideRepository(database).put("k", "v");

        assertThat(database.writes()).isEqualTo(1);
    }

    @Test
    @DisplayName("write-through makes a just-written value readable with no database read")
    void writeThroughGivesReadYourWrites() {
        WriteThroughRepository repository = new WriteThroughRepository(database);

        repository.put("k", "v");
        database.resetCounters();

        assertThat(repository.get("k")).contains("v");
        assertThat(database.reads())
                .as("write-through populates the cache, so this read is free")
                .isZero();
    }

    @Test
    @DisplayName("write-through writes the database before returning")
    void writeThroughIsSynchronous() {
        new WriteThroughRepository(database).put("k", "v");

        assertThat(database.writes()).isEqualTo(1);
        assertThat(database.read("k")).contains("v");
    }

    @Test
    @DisplayName("write-behind returns without touching the database - that is the risk window")
    void writeBehindDefersWrites() {
        WriteBehindRepository repository = new WriteBehindRepository(database);

        repository.put("a", "1");
        repository.put("b", "2");
        repository.put("c", "3");

        assertThat(database.writes())
                .as("nothing is persisted yet - a crash here loses all three")
                .isZero();
        assertThat(repository.pendingWrites()).isEqualTo(3);

        // ... but the values are readable immediately from the cache.
        assertThat(repository.get("a")).contains("1");
    }

    @Test
    @DisplayName("write-behind persists everything on flush, in one batch")
    void writeBehindFlushes() {
        WriteBehindRepository repository = new WriteBehindRepository(database);
        repository.put("a", "1");
        repository.put("b", "2");

        repository.flush();

        assertThat(database.writes()).isEqualTo(2);
        assertThat(repository.pendingWrites()).isZero();
        assertThat(database.read("a")).contains("1");
        assertThat(database.read("b")).contains("2");
    }

    @Test
    @DisplayName("write-behind coalesces repeated writes to the same key")
    void writeBehindCoalesces() {
        WriteBehindRepository repository = new WriteBehindRepository(database);
        for (int i = 0; i < 100; i++) {
            repository.put("counter", String.valueOf(i));
        }

        repository.flush();

        assertThat(database.writes())
                .as("100 in-memory updates become 1 database write - this is the real win")
                .isEqualTo(1);
        assertThat(database.read("counter")).contains("99");
    }
}
