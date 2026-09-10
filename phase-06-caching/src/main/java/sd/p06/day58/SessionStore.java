package sd.p06.day58;

import java.util.Optional;

/**
 * Where session state lives. The whole of today's lesson is in which implementation you pick.
 */
public interface SessionStore {

    void save(Session session);

    Optional<Session> load(String sessionId);

    void delete(String sessionId);

    String kind();
}
