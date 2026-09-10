package sd.p06.day58;

import java.util.Optional;

/** GIVEN - one application server. Its only state is whatever its SessionStore holds. */
public final class AppInstance {

    private final String instanceId;
    private final SessionStore sessions;

    public AppInstance(String instanceId, SessionStore sessions) {
        this.instanceId = instanceId;
        this.sessions = sessions;
    }

    public Session login(String sessionId, String userId) {
        Session session = Session.forUser(sessionId, userId);
        sessions.save(session);
        return session;
    }

    /** The user behind this session, or empty if this instance cannot resolve it. */
    public Optional<String> whoAmI(String sessionId) {
        return sessions.load(sessionId).map(Session::userId);
    }

    public void logout(String sessionId) {
        sessions.delete(sessionId);
    }

    public String instanceId() {
        return instanceId;
    }
}
