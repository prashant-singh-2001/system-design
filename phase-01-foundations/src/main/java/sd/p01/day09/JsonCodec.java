package sd.p01.day09;

/**
 * TODO(day09): hand-rolled JSON. No library - writing one is the point.
 *
 * <p>Produce exactly this shape (whitespace is up to you):
 *
 * <pre>
 *   {"id":123,"type":"user.created","timestampMillis":1735689600000,"payload":"..."}
 * </pre>
 *
 * <p>Encoding: build the string, then {@code getBytes(StandardCharsets.UTF_8)}. Escape at
 * minimum {@code "} and {@code \} inside string values. (Ask yourself what happens to a
 * payload containing a quote if you skip this - that is an injection bug, not a typo.)
 *
 * <p>Decoding: you only have to parse this fixed shape, not arbitrary JSON. Finding each
 * key and reading the value up to the next delimiter is perfectly acceptable here; a real
 * parser is a different exercise.
 *
 * <p>What JSON buys: any language can read it, humans can debug it, and adding a field does
 * not break old readers. What it costs: every field name is repeated in every single message,
 * and numbers are stored as text. At a million events a second, that repetition is most of
 * your bandwidth bill. That trade is why Protobuf and Avro exist.
 */
public final class JsonCodec implements Codec {
    @Override
    public byte[] encode(Event event) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(event.id()).append(",");
        sb.append("\"type\":\"").append(escapeJson(event.type())).append("\",");
        sb.append("\"timestampMillis\":").append(event.timestampMillis()).append(",");
        sb.append("\"payload\":\"").append(escapeJson(event.payload())).append("\"");
        sb.append("}");
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    public Event decode(byte[] bytes) {
        String json = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        long id = Long.parseLong(json.split("\"id\":")[1].split(",")[0]);
        String type = readStringValue(json, "\"type\":\"");
        long timestampMillis = Long.parseLong(json.split("\"timestampMillis\":")[1].split(",")[0]);
        String payload = readStringValue(json, "\"payload\":\"");
        return new Event(id, type, timestampMillis, payload);
    }

    /**
     * Reads a JSON string value starting right after {@code key}, stopping at the first
     * UNESCAPED quote - a plain {@code split("\"")} would stop at an escaped {@code \"}
     * instead, truncating any value that contains a quote or backslash.
     */
    private static String readStringValue(String json, String key) {
        int start = json.indexOf(key) + key.length();
        StringBuilder value = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                value.append(json.charAt(i + 1));
                i++;
            } else if (c == '"') {
                break;
            } else {
                value.append(c);
            }
        }
        return value.toString();
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public String name() {
        return "json";
    }
}
