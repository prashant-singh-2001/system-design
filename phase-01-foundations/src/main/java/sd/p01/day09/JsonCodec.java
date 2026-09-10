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
        throw new UnsupportedOperationException("TODO(day09): implement JSON encoding");
    }

    @Override
    public Event decode(byte[] bytes) {
        throw new UnsupportedOperationException("TODO(day09): implement JSON decoding");
    }

    @Override
    public String name() {
        return "json";
    }
}
