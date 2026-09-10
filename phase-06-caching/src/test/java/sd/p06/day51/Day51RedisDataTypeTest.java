package sd.p06.day51;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** A real Redis, started and thrown away by Testcontainers. Docker must be running. */
@Testcontainers
class Day51RedisDataTypeTest {

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    private static RedisClient client;
    private static StatefulRedisConnection<String, String> connection;
    private static RedisCommands<String, String> redis;
    private RedisDataTypeLab lab;

    @BeforeAll
    static void connect() {
        client = RedisClient.create(
                "redis://" + REDIS.getHost() + ":" + REDIS.getFirstMappedPort());
        connection = client.connect();
        redis = connection.sync();
    }

    @AfterAll
    static void disconnect() {
        connection.close();
        client.shutdown();
    }

    @BeforeEach
    void reset() {
        redis.flushall();
        lab = new RedisDataTypeLab(redis);
    }

    @Test
    @DisplayName("HASH: a profile round-trips, and one field updates without a read")
    void hashForProfiles() {
        lab.saveProfile("42", Map.of("name", "Alice", "city", "London", "plan", "free"));

        assertThat(lab.loadProfile("42"))
                .containsEntry("name", "Alice")
                .containsEntry("plan", "free");

        lab.updateProfileField("42", "plan", "pro");

        assertThat(lab.loadProfile("42"))
                .as("updating one field must not disturb the others")
                .containsEntry("plan", "pro")
                .containsEntry("name", "Alice")
                .containsEntry("city", "London");
    }

    @Test
    @DisplayName("HASH: the data really is a hash, not a serialized blob")
    void reallyUsesAHash() {
        lab.saveProfile("42", Map.of("name", "Alice"));

        assertThat(redis.type("user:42"))
                .as("a JSON string would work but makes single-field updates read-modify-write")
                .isEqualTo("hash");
    }

    @Test
    @DisplayName("SORTED SET: the leaderboard comes back ordered, highest first")
    void sortedSetForLeaderboard() {
        lab.recordScore("chess", "alice", 1500);
        lab.recordScore("chess", "bob", 2200);
        lab.recordScore("chess", "carol", 1800);
        lab.recordScore("chess", "dave", 900);

        assertThat(lab.topPlayers("chess", 3)).containsExactly("bob", "carol", "alice");
    }

    @Test
    @DisplayName("SORTED SET: re-recording a score updates it rather than duplicating")
    void scoresAreUpdated() {
        lab.recordScore("chess", "alice", 1500);
        lab.recordScore("chess", "alice", 2500);

        assertThat(lab.topPlayers("chess", 10)).containsExactly("alice");
    }

    @Test
    @DisplayName("HYPERLOGLOG: approximate cardinality in fixed memory")
    void hyperLogLogForUniques() {
        for (int i = 0; i < 10_000; i++) {
            lab.recordVisitor("2026-03-01", "visitor-" + i);
        }
        lab.recordVisitor("2026-03-01", "visitor-0");        // a repeat must not count twice

        long counted = lab.uniqueVisitors("2026-03-01");
        System.out.printf("  10,000 visitors -> HyperLogLog says %,d (%.2f%% error)%n",
                counted, Math.abs(counted - 10_000) / 100.0);

        assertThat(counted)
                .as("HyperLogLog trades ~0.81%% error for constant memory")
                .isBetween(9_700L, 10_300L);
    }

    @Test
    @DisplayName("INCR + EXPIRE: the window resets rather than sliding forever")
    void counterExpiresCorrectly() {
        assertThat(lab.incrementRequestCount("client-1", 60)).isEqualTo(1);
        assertThat(lab.incrementRequestCount("client-1", 60)).isEqualTo(2);
        assertThat(lab.incrementRequestCount("client-1", 60)).isEqualTo(3);

        long ttl = redis.ttl("ratelimit:client-1");
        assertThat(ttl).as("a TTL must have been set on creation").isBetween(1L, 60L);

        // Calling EXPIRE on every request would push the deadline forward forever and the
        // window would never reset. Set it only when the counter is created.
        redis.set("ratelimit:client-2", "5");
        redis.expire("ratelimit:client-2", 10);
        lab.incrementRequestCount("client-2", 60);

        assertThat(redis.ttl("ratelimit:client-2"))
                .as("an existing window's expiry must not be extended by a new request")
                .isLessThanOrEqualTo(10);
    }
}
