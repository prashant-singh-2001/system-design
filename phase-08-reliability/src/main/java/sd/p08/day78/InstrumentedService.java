package sd.p08.day78;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * TODO(day78): instrument a service so you can answer questions about it in production.
 *
 * <p>The three pillars, and what each is actually for:
 * <ul>
 *   <li><b>Metrics</b> - cheap, aggregated numbers. "How many requests, how fast, how many
 *       errors." Constant cost regardless of traffic, so you can have them always on. They tell
 *       you <em>that</em> something is wrong.</li>
 *   <li><b>Logs</b> - detailed, per-event, expensive at volume. They tell you <em>what</em>
 *       happened in one case.</li>
 *   <li><b>Traces</b> - one request's path across services. They tell you <em>where</em> the time
 *       went.</li>
 * </ul>
 *
 * <p>Metrics first, because they are the only one you can afford to have on for everything.
 *
 * <p>The instrument types matter, and the wrong choice is a common and expensive mistake:
 * <ul>
 *   <li><b>Counter</b> - monotonically increasing. Requests, errors, bytes. You query the RATE,
 *       not the value.</li>
 *   <li><b>Gauge</b> - a value that goes up and down. Queue depth, connections, memory.</li>
 *   <li><b>Timer</b> - duration plus count, and it can publish <b>percentiles</b>. Use this for
 *       latency, always. A "mean latency" gauge is the mistake: as Day 60 showed, the mean
 *       describes nobody's experience, and once you have averaged you can never recover the tail.</li>
 * </ul>
 *
 * <p><b>Tags</b> are the real power - a counter tagged by endpoint and status lets you ask
 * questions nobody anticipated. And they are the real danger: every distinct tag combination is a
 * separate time series. Tag by user id and you have created millions of series, a phenomenon with
 * its own name - <b>cardinality explosion</b> - and a memorable way to take down your monitoring
 * system with a one-line change. Tag by things with bounded values: endpoint, status, region.
 * Never by ids.
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code handleRequest} - time the call with a {@link Timer} tagged
 *       {@code endpoint} and {@code outcome} ({@code success} / {@code error}), record the
 *       duration whichever way it goes, and rethrow on failure. A metric that is only recorded on
 *       success hides exactly the incident you needed it for.</li>
 *   <li>{@code recordQueueDepth} - register a gauge over the supplied supplier.</li>
 * </ul>
 */
public final class InstrumentedService {

    public static final String REQUEST_TIMER = "http.server.requests";
    public static final String QUEUE_DEPTH_GAUGE = "worker.queue.depth";

    private final MeterRegistry registry;

    public InstrumentedService(MeterRegistry registry) {
        this.registry = registry;
    }

    public <T> T handleRequest(String endpoint, Callable<T> handler) {
        throw new UnsupportedOperationException("TODO(day78): time it, tag it, record both outcomes");
    }

    public void recordQueueDepth(String queueName, java.util.function.Supplier<Number> depth) {
        throw new UnsupportedOperationException("TODO(day78): register a gauge");
    }

    // ---------------------------------------------------------------- given

    /** Total count for one endpoint/outcome combination, or 0 if that series does not exist. */
    public long requestCount(String endpoint, String outcome) {
        Timer timer = registry.find(REQUEST_TIMER)
                .tag("endpoint", endpoint).tag("outcome", outcome).timer();
        return timer == null ? 0 : timer.count();
    }

    public Duration maxLatency(String endpoint, String outcome) {
        Timer timer = registry.find(REQUEST_TIMER)
                .tag("endpoint", endpoint).tag("outcome", outcome).timer();
        return timer == null ? Duration.ZERO
                : Duration.ofNanos((long) timer.max(java.util.concurrent.TimeUnit.NANOSECONDS));
    }

    public MeterRegistry registry() {
        return registry;
    }
}
