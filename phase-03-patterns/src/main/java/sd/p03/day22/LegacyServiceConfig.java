package sd.p03.day22;

/**
 * GIVEN, and the before-picture. A telescoping constructor: every combination of "optional"
 * parameters needs its own overload, and every overload just forwards to the widest one with
 * made-up defaults filling the gaps.
 *
 * <p>Read the four-argument call site below and try to say, without checking this file, which
 * argument is the port and which is the timeout. You cannot - they are both {@code int}, and
 * nothing at the call site names them. Swap two arguments of the same type and the code still
 * compiles, still runs, and quietly connects to the wrong host with the wrong timeout.
 *
 * <pre>
 *   new LegacyServiceConfig("payments.internal", 8080, 3000, 10);
 * </pre>
 *
 * <p>This is the problem a {@link ServiceConfig.Builder} exists to solve: name every value at
 * the call site, validate the whole thing in one place, and make the object impossible to
 * construct half-finished.
 */
public final class LegacyServiceConfig {

    private final String host;
    private final int port;
    private final long timeoutMs;
    private final int maxConnections;

    public LegacyServiceConfig(String host, int port) {
        this(host, port, 5_000, 4);
    }

    public LegacyServiceConfig(String host, int port, long timeoutMs) {
        this(host, port, timeoutMs, 4);
    }

    public LegacyServiceConfig(String host, int port, long timeoutMs, int maxConnections) {
        this.host = host;
        this.port = port;
        this.timeoutMs = timeoutMs;
        this.maxConnections = maxConnections;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public long timeoutMs() {
        return timeoutMs;
    }

    public int maxConnections() {
        return maxConnections;
    }
}
