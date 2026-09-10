package sd.p01.day09;

/**
 * TODO(day09): a compact binary format using {@code DataOutputStream} / {@code DataInputStream}.
 *
 * <p>Write the fields in a fixed order and read them back in the same order:
 *
 * <pre>
 *   out.writeLong(event.id());
 *   out.writeUTF(event.type());            // length-prefixed UTF-8
 *   out.writeLong(event.timestampMillis());
 *   out.writeUTF(event.payload());
 * </pre>
 *
 * <p>There are no field names on the wire at all. The reader knows the layout, so a long is
 * 8 bytes instead of up to 20 characters of text, and {@code "timestampMillis"} is never
 * transmitted. Expect roughly a third of the JSON size.
 *
 * <p>The cost is the thing to internalise: the format is now IMPLICIT in the code. Reader and
 * writer must agree exactly, forever. Add a field and old readers break; reorder two fields
 * and you get silent corruption rather than an error. Schema systems like Protobuf and Avro
 * exist to give you this density back WITH a way to evolve - field tags, or a schema registry.
 *
 * <p>Once it works, answer this: how would you add a fifth field without breaking every
 * existing reader? That question is the whole of schema evolution.
 */
public final class BinaryCodec implements Codec {

    @Override
    public byte[] encode(Event event) {
        throw new UnsupportedOperationException("TODO(day09): implement binary encoding");
    }

    @Override
    public Event decode(byte[] bytes) {
        throw new UnsupportedOperationException("TODO(day09): implement binary decoding");
    }

    @Override
    public String name() {
        return "binary";
    }
}
