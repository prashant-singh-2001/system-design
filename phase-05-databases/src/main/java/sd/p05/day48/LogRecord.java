package sd.p05.day48;

/** One durable fact: an operation, a key, and (for PUT) a value. */
public record LogRecord(String operation, String key, String value) {
}
