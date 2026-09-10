package sd.p06.day58;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GIVEN - session state held in the application process. The default in every framework, and the
 * reason sticky sessions exist.
 *
 * <p>It is fast and simple, and it makes your instances non-interchangeable. Instance A knows
 * about a session that instances B and C have never heard of, so the load balancer must keep
 * sending that user back to A forever. That single constraint causes a cascade of operational
 * pain:
 *
 * <ul>
 *   <li><b>Deploys log people out.</b> Restarting A destroys its sessions.</li>
 *   <li><b>Autoscaling is lopsided.</b> New instances get only new users, so a scale-out under
 *       load does not relieve the instance that is actually overloaded.</li>
 *   <li><b>Losing an instance loses its users' state</b>, not just its capacity.</li>
 *   <li><b>Load balancing degrades.</b> Least-connections cannot move a pinned client, so your
 *       clever balancing strategy stops applying to exactly the traffic that needs it.</li>
 * </ul>
 *
 * <p>Note that none of these is a bug. This class is correct. The problem is architectural: state
 * in the instance is what makes the instance special.
 */
public final class InMemorySessionStore implements SessionStore {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    @Override
    public void save(Session session) {
        sessions.put(session.id(), session);
    }

    @Override
    public Optional<Session> load(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public void delete(String sessionId) {
        sessions.remove(sessionId);
    }

    @Override
    public String kind() {
        return "in-memory (requires sticky sessions)";
    }
}
