package sd.p01.day05;

/** Four implementations, identical semantics, wildly different behaviour under contention. */
public interface Counter {

    void increment();

    long value();

    /** For readable benchmark output. */
    String strategy();
}
