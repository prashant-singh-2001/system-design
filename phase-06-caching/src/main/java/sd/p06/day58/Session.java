package sd.p06.day58;

import java.util.Map;

public record Session(String id, String userId, Map<String, String> attributes) {

    public Session {
        attributes = Map.copyOf(attributes);
    }

    public static Session forUser(String id, String userId) {
        return new Session(id, userId, Map.of());
    }
}
