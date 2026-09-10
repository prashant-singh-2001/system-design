package sd.p06.day55;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Day 55 - one application instance with a LOCAL cache, kept coherent across the fleet.
 *
 * <p>A local in-process cache is the fastest cache there is: no network, nanoseconds. Run three
 * instances behind a load balancer and you now have three copies of every value and no way to
 * update them together. Instance A writes, instances B and C keep serving the old value until
 * their TTL runs out. Users see the change, refresh, and see it vanish.
 *
 * <p>The standard remedy is a broadcast invalidation channel: whoever changes the data publishes
 * the key, and every instance drops its local copy. This is what Hibernate's second-level cache
 * clustering, Caffeine plus a message bus, and Redis client-side caching all do underneath.
 *
 * <p>Be clear about what this does and does not give you. It is <b>eventual</b> consistency with
 * a small window - the message takes a millisecond or two, and during that window instance C is
 * still serving stale data. Pub/sub is also fire-and-forget: an instance that is down or
 * disconnected never gets the message and stays stale until its TTL saves it. So a TTL remains
 * your backstop even with invalidation in place. Anyone who tells you broadcast invalidation
 * makes a distributed cache strongly consistent has not thought about the partition.
 */
public final class CacheNode implements AutoCloseable {

    private final String nodeId;
    private final String channel;
    private final Map<String, String> local = new ConcurrentHashMap<>();
    private final StatefulRedisConnection<String, String> publisher;
    private final StatefulRedisPubSubConnection<String, String> subscriber;
    private final AtomicInteger invalidationsReceived = new AtomicInteger();

    /** GIVEN - the Lettuce pub/sub wiring. The lesson is below, not here. */
    public CacheNode(String nodeId, RedisClient client, String channel) {
        this.nodeId = nodeId;
        this.channel = channel;
        this.publisher = client.connect();
        this.subscriber = client.connectPubSub();

        subscriber.addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String receivedChannel, String message) {
                onInvalidationMessage(message);
            }
        });
        subscriber.sync().subscribe(channel);
    }

    /** Local write. Nothing is broadcast - only invalidation is. */
    public void put(String key, String value) {
        local.put(key, value);
    }

    /** Local read, at in-process speed. */
    public Optional<String> get(String key) {
        return Optional.ofNullable(local.get(key));
    }

    /**
     * TODO(day55): drop this key locally AND tell every other instance to do the same.
     *
     * <p>Two steps: remove from {@code local}, then publish on {@code channel} using
     * {@code publisher.sync().publish(...)}.
     *
     * <p>The message must carry both the originating node id and the key, formatted
     * {@code nodeId + "|" + key} - {@link #onInvalidationMessage} relies on that shape.
     *
     * <p>Invalidate rather than broadcasting the new VALUE. Day 52's rule holds even harder
     * here: with several publishers there is no ordering guarantee between two updates, so
     * broadcasting values means instances can converge on the older one. Deleting is idempotent
     * and order-independent, and the worst case is an extra miss.
     */
    public void invalidate(String key) {
        throw new UnsupportedOperationException("TODO(day55): evict locally, then publish");
    }

    /**
     * TODO(day55): handle an invalidation broadcast from anywhere in the fleet.
     *
     * <p>The message is {@code nodeId|key}. Split on the first {@code |}.
     *
     * <p>Ignore messages this node published itself - it has already evicted, and processing
     * your own broadcast is how echo loops start once somebody adds a "re-publish on receive"
     * feature. Count every message you do act on via {@code invalidationsReceived}.
     */
    private void onInvalidationMessage(String message) {
        throw new UnsupportedOperationException("TODO(day55): parse, skip self, evict, count");
    }

    public int invalidationsReceived() {
        return invalidationsReceived.get();
    }

    public String nodeId() {
        return nodeId;
    }

    @Override
    public void close() {
        subscriber.close();
        publisher.close();
    }
}
