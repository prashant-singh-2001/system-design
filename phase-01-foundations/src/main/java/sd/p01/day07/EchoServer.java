package sd.p01.day07;

/** A server that reads a newline-terminated message and writes it straight back. */
public interface EchoServer extends AutoCloseable {

    /** The ephemeral port actually bound. Tests use port 0 and ask afterwards. */
    int port();

    /** How this server handles concurrency - for readable test output. */
    String model();

    @Override
    void close();
}
