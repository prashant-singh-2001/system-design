package sd.p06.day55;

import io.lettuce.core.RedisClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class Day55InvalidationTest {

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    private static RedisClient client;

    @BeforeAll
    static void connect() {
        client = RedisClient.create(
                "redis://" + REDIS.getHost() + ":" + REDIS.getFirstMappedPort());
    }

    @AfterAll
    static void disconnect() {
        client.shutdown();
    }

    /** Pub/sub is asynchronous, so poll rather than sleep a fixed amount. */
    private static void eventually(BooleanSupplier condition, String description) {
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new AssertionError("timed out waiting for: " + description);
    }

    @Test
    @DisplayName("an invalidation on one instance evicts the key on every other instance")
    void invalidationPropagates() {
        String channel = "invalidate:test-1";
        try (CacheNode a = new CacheNode("a", client, channel);
             CacheNode b = new CacheNode("b", client, channel);
             CacheNode c = new CacheNode("c", client, channel)) {

            // All three instances have independently cached the same row.
            a.put("user:1", "Alice");
            b.put("user:1", "Alice");
            c.put("user:1", "Alice");

            // Instance A handles the write and broadcasts.
            a.invalidate("user:1");

            assertThat(a.get("user:1")).as("the publisher evicts synchronously").isEmpty();
            eventually(() -> b.get("user:1").isEmpty(), "instance b to evict");
            eventually(() -> c.get("user:1").isEmpty(), "instance c to evict");
        }
    }

    @Test
    @DisplayName("only the named key is dropped - invalidation is not a flush")
    void onlyTheNamedKeyIsEvicted() {
        String channel = "invalidate:test-2";
        try (CacheNode a = new CacheNode("a", client, channel);
             CacheNode b = new CacheNode("b", client, channel)) {

            b.put("user:1", "Alice");
            b.put("user:2", "Bob");
            b.put("product:9", "Widget");

            a.invalidate("user:1");

            eventually(() -> b.get("user:1").isEmpty(), "user:1 to be evicted on b");
            assertThat(b.get("user:2")).as("unrelated keys must survive").contains("Bob");
            assertThat(b.get("product:9")).contains("Widget");
        }
    }

    @Test
    @DisplayName("a node ignores its own broadcast - this is how echo loops are prevented")
    void selfMessagesAreIgnored() {
        String channel = "invalidate:test-3";
        try (CacheNode a = new CacheNode("a", client, channel);
             CacheNode b = new CacheNode("b", client, channel)) {

            a.put("k", "v");
            a.invalidate("k");

            eventually(() -> b.invalidationsReceived() == 1, "b to receive one message");

            assertThat(a.invalidationsReceived())
                    .as("A published it and already evicted; reprocessing it is how loops start")
                    .isZero();
        }
    }

    @Test
    @DisplayName("every instance can publish - invalidation is not one-directional")
    void anyNodeCanInvalidate() {
        String channel = "invalidate:test-4";
        try (CacheNode a = new CacheNode("a", client, channel);
             CacheNode b = new CacheNode("b", client, channel)) {

            a.put("k", "v");
            b.put("k", "v");

            b.invalidate("k");

            eventually(() -> a.get("k").isEmpty(), "a to evict after b published");
            assertThat(b.get("k")).isEmpty();
        }
    }

    @Test
    @DisplayName("invalidating a key nobody cached is harmless")
    void invalidatingAnAbsentKeyIsSafe() {
        String channel = "invalidate:test-5";
        try (CacheNode a = new CacheNode("a", client, channel);
             CacheNode b = new CacheNode("b", client, channel)) {

            a.invalidate("never-cached");

            eventually(() -> b.invalidationsReceived() == 1, "b to receive the message");
            assertThat(b.get("never-cached")).isEqualTo(Optional.empty());
        }
    }
}
