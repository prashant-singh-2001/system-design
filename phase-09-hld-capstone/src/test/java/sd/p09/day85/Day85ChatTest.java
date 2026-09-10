package sd.p09.day85;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sd.p09.support.MutableClock;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class Day85ChatTest {

    private static final Duration TTL = Duration.ofSeconds(30);

    private MutableClock clock;
    private PresenceRegistry presence;
    private MessageRouter router;

    @BeforeEach
    void setUp() {
        clock = MutableClock.startingAt("2026-03-01T12:00:00Z");
        presence = new PresenceRegistry(clock);
        router = new MessageRouter(presence);
    }

    @Test
    @DisplayName("a connected user is routable to their server")
    void connectAndRoute() {
        presence.connect("alice", "chat-7", TTL);

        assertThat(presence.serverFor("alice")).contains("chat-7");
        assertThat(presence.onlineUsers()).containsExactly("alice");
    }

    @Test
    @DisplayName("an unknown user is simply absent")
    void unknownUser() {
        assertThat(presence.serverFor("nobody")).isEmpty();
    }

    @Test
    @DisplayName("presence expires - a crashed server cannot deregister its users")
    void presenceExpires() {
        presence.connect("alice", "chat-7", TTL);

        clock.advance(Duration.ofSeconds(31));

        assertThat(presence.serverFor("alice"))
                .as("""
                        A server that dies takes its connections with it and never tells anybody.
                        Only a TTL cleans that up, which means presence is always slightly wrong -
                        and the design has to tolerate that.""")
                .isEmpty();
        assertThat(presence.onlineUsers()).isEmpty();
    }

    @Test
    @DisplayName("a heartbeat keeps the route alive")
    void heartbeatExtends() {
        presence.connect("alice", "chat-7", TTL);

        clock.advance(Duration.ofSeconds(20));
        assertThat(presence.heartbeat("alice", TTL)).isTrue();
        clock.advance(Duration.ofSeconds(20));

        assertThat(presence.serverFor("alice")).contains("chat-7");
    }

    @Test
    @DisplayName("a heartbeat cannot resurrect an expired route")
    void heartbeatOnExpired() {
        presence.connect("alice", "chat-7", TTL);
        clock.advance(Duration.ofSeconds(31));

        assertThat(presence.heartbeat("alice", TTL))
                .as("the user must reconnect - reviving a stale route sends messages nowhere")
                .isFalse();
    }

    @Test
    @DisplayName("reconnecting to a different server updates the route")
    void reconnectElsewhere() {
        presence.connect("alice", "chat-7", TTL);
        presence.connect("alice", "chat-2", TTL);

        assertThat(presence.serverFor("alice")).contains("chat-2");
    }

    @Test
    @DisplayName("an online recipient is delivered to their server")
    void deliverOnline() {
        presence.connect("bob", "chat-3", TTL);

        MessageRouter.RoutingResult result = router.send("bob", "hello");

        assertThat(result.deliveredToServers()).containsExactly("chat-3");
        assertThat(result.queuedForUsers()).isEmpty();
    }

    @Test
    @DisplayName("an offline recipient is queued, not dropped")
    void queueOffline() {
        MessageRouter.RoutingResult result = router.send("bob", "hello");

        assertThat(result.deliveredToServers()).isEmpty();
        assertThat(result.queuedForUsers())
                .as("""
                        This is the case that makes chat feel reliable, and it is where most of the
                        storage goes. A design that only handles the online path is not a chat
                        system, it is a broadcast.""")
                .containsExactly("bob");
        assertThat(router.offlineQueue()).containsExactly("bob");
    }

    @Test
    @DisplayName("a group send is Day 84's fanout in a different costume")
    void groupFanout() {
        presence.connect("a", "chat-1", TTL);
        presence.connect("b", "chat-2", TTL);
        presence.connect("c", "chat-1", TTL);

        MessageRouter.RoutingResult result =
                router.sendToGroup(List.of("a", "b", "c", "d", "e"), "hi all");

        assertThat(result.deliveredToServers())
                .as("one send, three deliveries - scale this to a 500-member group")
                .containsExactly("chat-1", "chat-2", "chat-1");
        assertThat(result.queuedForUsers()).containsExactly("d", "e");
    }

    @Test
    @DisplayName("disconnecting removes the route immediately")
    void disconnect() {
        presence.connect("alice", "chat-7", TTL);
        presence.disconnect("alice");

        assertThat(presence.serverFor("alice")).isEmpty();
        assertThat(router.send("alice", "hi").queuedForUsers()).containsExactly("alice");
    }
}
