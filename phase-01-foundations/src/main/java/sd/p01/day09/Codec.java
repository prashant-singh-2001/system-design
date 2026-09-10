package sd.p01.day09;

/** Encode an event to bytes and get the same event back. */
public interface Codec {

    byte[] encode(Event event);

    Event decode(byte[] bytes);

    String name();
}
