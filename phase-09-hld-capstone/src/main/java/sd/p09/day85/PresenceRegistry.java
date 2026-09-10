package sd.p09.day85;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * TODO(day85): who is online, and on which server.
 *
 * <p>Chat is the first design in this phase where the connection itself is state. HTTP is
 * request-response and stateless; a WebSocket is a long-lived connection pinned to one server. So
 * "deliver this message to Bob" first requires knowing <b>which of your fifty servers Bob is
 * connected to</b>, and that mapping changes constantly.
 *
 * <p>That is why chat needs a presence registry - a shared, fast, expiring map from user to
 * server. Redis, typically, because the access pattern is exactly a hash with a TTL (Day 51).
 *
 * <p>The TTL is doing real work. A server that crashes cannot deregister its users, so entries
 * must expire on their own; clients refresh with a heartbeat. Presence is therefore always
 * slightly wrong, and the design has to tolerate that - a message routed to a server that no
 * longer holds the user must fall back to offline delivery rather than vanish.
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code connect} - record user -> server with an expiry {@code ttl} from now.</li>
 *   <li>{@code serverFor} - the server holding this user, or empty if absent or expired.</li>
 *   <li>{@code heartbeat} - extend an existing entry; return false if there was nothing to extend
 *       (the user must reconnect rather than resurrect a stale route).</li>
 *   <li>{@code disconnect} - remove the entry.</li>
 *   <li>{@code onlineUsers} - every user whose entry has not expired.</li>
 * </ul>
 */
public final class PresenceRegistry {

    private record Presence(String serverId, Instant expiresAt) {
    }

    private final Map<String, Presence> presence = new HashMap<>();
    private final Clock clock;

    public PresenceRegistry(Clock clock) {
        this.clock = clock;
    }

    public void connect(String userId, String serverId, Duration ttl) {
        throw new UnsupportedOperationException("TODO(day85): record the route with an expiry");
    }

    public java.util.Optional<String> serverFor(String userId) {
        throw new UnsupportedOperationException("TODO(day85): the holding server, if still live");
    }

    public boolean heartbeat(String userId, Duration ttl) {
        throw new UnsupportedOperationException("TODO(day85): extend, or report nothing to extend");
    }

    public void disconnect(String userId) {
        throw new UnsupportedOperationException("TODO(day85): drop the route");
    }

    public Set<String> onlineUsers() {
        throw new UnsupportedOperationException("TODO(day85): everyone not yet expired");
    }
}
