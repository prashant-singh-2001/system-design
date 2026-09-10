package sd.p09.day85;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO(day85): route a message, and decide what happens when the recipient is not there.
 *
 * <p>Three outcomes, and a design that only handles the first is not a chat system:
 * <ul>
 *   <li><b>DELIVERED</b> - the recipient is online; route to their server.</li>
 *   <li><b>QUEUED</b> - the recipient is offline; store for delivery on reconnect. This is the
 *       case that makes chat feel reliable, and it is where most of the storage goes.</li>
 *   <li><b>FANNED_OUT</b> - a group message, delivered to each online member and queued for the
 *       rest. Note the cost: a 500-member group turns one send into 500 deliveries, which is
 *       Day 84's fanout problem in a different costume.</li>
 * </ul>
 *
 * <p>Implement {@code send} (one recipient) and {@code sendToGroup}, returning a
 * {@link RoutingResult} that records where the message went and what was queued.
 */
public final class MessageRouter {

    public record RoutingResult(List<String> deliveredToServers, List<String> queuedForUsers) {
    }

    private final PresenceRegistry presence;
    private final List<String> offlineQueue = new ArrayList<>();

    public MessageRouter(PresenceRegistry presence) {
        this.presence = presence;
    }

    public RoutingResult send(String recipientId, String message) {
        throw new UnsupportedOperationException("TODO(day85): deliver if online, queue if not");
    }

    public RoutingResult sendToGroup(List<String> memberIds, String message) {
        throw new UnsupportedOperationException("TODO(day85): fan out to each member");
    }

    public List<String> offlineQueue() {
        return List.copyOf(offlineQueue);
    }
}
