package sd.p07.day67;

public record Message(String id, String payload, int attempts) {

    public Message retried() {
        return new Message(id, payload, attempts + 1);
    }

    public static Message of(String id, String payload) {
        return new Message(id, payload, 0);
    }
}
