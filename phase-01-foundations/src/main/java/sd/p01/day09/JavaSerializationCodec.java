package sd.p01.day09;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * GIVEN - the convenient one, and the one you should almost never ship.
 *
 * <p>Every payload carries the class descriptor, so the bytes are bloated. It is Java-only,
 * so nothing else can read your data. It is brittle across versions. And deserializing
 * untrusted bytes is a remote code execution vector - this is the mechanism behind a long
 * list of real CVEs.
 *
 * <p>It is here as the baseline you measure the others against.
 */
public final class JavaSerializationCodec implements Codec {

    @Override
    public byte[] encode(Event event) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(buffer)) {
            out.writeObject(event);
        } catch (IOException e) {
            throw new IllegalStateException("encode failed", e);
        }
        return buffer.toByteArray();
    }

    @Override
    public Event decode(byte[] bytes) {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (Event) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("decode failed", e);
        }
    }

    @Override
    public String name() {
        return "java-serialization";
    }
}
