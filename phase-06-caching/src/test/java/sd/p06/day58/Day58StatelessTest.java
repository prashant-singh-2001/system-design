package sd.p06.day58;

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

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class Day58StatelessTest {

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    private static RedisClient client;
    private static StatefulRedisConnection<String, String> connection;
    private static RedisCommands<String, String> redis;

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
    }

    private RedisSessionStore sharedStore() {
        return new RedisSessionStore(redis, Duration.ofMinutes(30));
    }

    @Test
    @DisplayName("the before-picture: in-memory sessions make instances non-interchangeable")
    void inMemorySessionsRequireStickiness() {
        AppInstance a = new AppInstance("app-a", new InMemorySessionStore());
        AppInstance b = new AppInstance("app-b", new InMemorySessionStore());

        a.login("sess-1", "alice");

        assertThat(a.whoAmI("sess-1")).contains("alice");
        assertThat(b.whoAmI("sess-1"))
                .as("""
                        Instance B has never heard of this session. The load balancer must now pin
                        this user to A forever - and that single constraint is what breaks deploys,
                        autoscaling and least-connections balancing.""")
                .isEmpty();
    }

    @Test
    @DisplayName("with a shared store, ANY instance can serve the request")
    void sharedSessionsMakeInstancesInterchangeable() {
        AppInstance a = new AppInstance("app-a", sharedStore());
        AppInstance b = new AppInstance("app-b", sharedStore());
        AppInstance c = new AppInstance("app-c", sharedStore());

        a.login("sess-1", "alice");

        assertThat(a.whoAmI("sess-1")).contains("alice");
        assertThat(b.whoAmI("sess-1")).as("no stickiness needed").contains("alice");
        assertThat(c.whoAmI("sess-1")).contains("alice");
    }

    @Test
    @DisplayName("losing an instance no longer loses its users")
    void instanceLossIsSurvivable() {
        AppInstance a = new AppInstance("app-a", sharedStore());
        a.login("sess-1", "alice");

        // "app-a" dies. A brand-new instance comes up in its place.
        AppInstance replacement = new AppInstance("app-d", sharedStore());

        assertThat(replacement.whoAmI("sess-1"))
                .as("a deploy or a crash is now a capacity event, not a logout event")
                .contains("alice");
    }

    @Test
    @DisplayName("logout on one instance is a logout everywhere")
    void logoutPropagates() {
        AppInstance a = new AppInstance("app-a", sharedStore());
        AppInstance b = new AppInstance("app-b", sharedStore());

        a.login("sess-1", "alice");
        b.logout("sess-1");

        assertThat(a.whoAmI("sess-1")).isEmpty();
        assertThat(b.whoAmI("sess-1")).isEmpty();
    }

    @Test
    @DisplayName("an unknown session is absent, not an error")
    void unknownSession() {
        assertThat(sharedStore().load("never-existed")).isEmpty();
    }

    @Test
    @DisplayName("attributes survive the round trip")
    void attributesRoundTrip() {
        SessionStore store = sharedStore();
        store.save(new Session("sess-1", "alice",
                Map.of("theme", "dark", "locale", "en-GB")));

        assertThat(store.load("sess-1")).hasValueSatisfying(session -> {
            assertThat(session.userId()).isEqualTo("alice");
            assertThat(session.attributes())
                    .containsEntry("theme", "dark")
                    .containsEntry("locale", "en-GB");
        });
    }

    @Test
    @DisplayName("sessions expire on their own - a store with no TTL is a memory leak")
    void sessionsHaveATtl() {
        sharedStore().save(Session.forUser("sess-1", "alice"));

        assertThat(redis.ttl("session:sess-1"))
                .as("set the TTL on save, or sessions accumulate forever")
                .isBetween(1L, Duration.ofMinutes(30).toSeconds());
    }

    @Test
    @DisplayName("the session really is a hash, so one attribute can be updated in place")
    void storedAsAHash() {
        sharedStore().save(Session.forUser("sess-1", "alice"));

        assertThat(redis.type("session:sess-1")).isEqualTo("hash");
    }
}
