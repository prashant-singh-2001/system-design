package sd.p06.day58;

import io.lettuce.core.api.sync.RedisCommands;

import java.time.Duration;
import java.util.Optional;

/**
 * TODO(day58): move session state OUT of the instance, so every instance is interchangeable.
 *
 * <p>This one change - and it is a small one - is what makes a service horizontally scalable.
 * Any instance can serve any request, deploys stop logging people out, autoscaling works, and
 * the load balancer is free to use whatever strategy it likes.
 *
 * <p>Store each session as a Redis HASH under {@code session:<id>} (Day 51's choice, for Day 51's
 * reason - one attribute can be updated without rewriting the whole session):
 * <ul>
 *   <li>a {@code userId} field</li>
 *   <li>one field per attribute, prefixed {@code attr:} so it cannot collide with
 *       {@code userId}</li>
 * </ul>
 *
 * <p>Set the TTL on save. Sessions must expire on their own - a session store with no expiry is
 * a memory leak with a login page. Use {@code expire} after {@code hset}.
 *
 * <p>{@code load} returns empty when the key is gone: {@code hgetall} on a missing key returns an
 * EMPTY MAP rather than null, so check for emptiness, not for null. That difference is a real
 * NullPointerException-versus-silent-empty-session bug.
 *
 * <p>The trade-off you are accepting: every session read is now a network round trip, so you have
 * traded nanoseconds for milliseconds. That is almost always worth it - and where it is not, the
 * answer is a short-TTL local cache in front of this, which puts you straight back into Day 55's
 * invalidation problem. There is no free lunch, only a better-understood bill.
 */
public final class RedisSessionStore implements SessionStore {

    private final RedisCommands<String, String> redis;
    private final Duration ttl;

    public RedisSessionStore(RedisCommands<String, String> redis, Duration ttl) {
        this.redis = redis;
        this.ttl = ttl;
    }

    @Override
    public void save(Session session) {
        throw new UnsupportedOperationException("TODO(day58): hset the fields, then expire");
    }

    @Override
    public Optional<Session> load(String sessionId) {
        throw new UnsupportedOperationException("TODO(day58): hgetall, empty map means absent");
    }

    @Override
    public void delete(String sessionId) {
        throw new UnsupportedOperationException("TODO(day58): del the key");
    }

    @Override
    public String kind() {
        return "redis (stateless instances)";
    }
}
