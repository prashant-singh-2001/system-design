package sd.p08.day78;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day78ObservabilityTest {

    private MeterRegistry registry;
    private InstrumentedService service;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        service = new InstrumentedService(registry);
    }

    @Test
    @DisplayName("successful requests are counted and timed")
    void countsSuccesses() {
        for (int i = 0; i < 5; i++) {
            assertThat(service.handleRequest("/orders", () -> "ok")).isEqualTo("ok");
        }

        assertThat(service.requestCount("/orders", "success")).isEqualTo(5);
        assertThat(service.maxLatency("/orders", "success")).isPositive();
    }

    @Test
    @DisplayName("failures are recorded too - and the exception still propagates")
    void countsFailures() {
        assertThatThrownBy(() -> service.handleRequest("/orders", () -> {
            throw new IllegalStateException("boom");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(service.requestCount("/orders", "error"))
                .as("""
                        A metric recorded only on success hides exactly the incident you needed it
                        for. Record the duration whichever way the call goes.""")
                .isEqualTo(1);
        assertThat(service.requestCount("/orders", "success")).isZero();
    }

    @Test
    @DisplayName("tags let you ask questions nobody anticipated")
    void tagsSeparateSeries() {
        service.handleRequest("/orders", () -> "ok");
        service.handleRequest("/orders", () -> "ok");
        service.handleRequest("/users", () -> "ok");
        assertThatThrownBy(() -> service.handleRequest("/users", () -> {
            throw new IllegalStateException("boom");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(service.requestCount("/orders", "success")).isEqualTo(2);
        assertThat(service.requestCount("/users", "success")).isEqualTo(1);
        assertThat(service.requestCount("/users", "error")).isEqualTo(1);
        assertThat(service.requestCount("/orders", "error")).isZero();
    }

    @Test
    @DisplayName("a gauge reflects the current value, not a historical one")
    void gaugeTracksCurrentValue() {
        AtomicInteger depth = new AtomicInteger(3);
        service.recordQueueDepth("emails", depth::get);

        Gauge gauge = registry.find(InstrumentedService.QUEUE_DEPTH_GAUGE)
                .tag("queue", "emails").gauge();

        assertThat(gauge).isNotNull();
        assertThat(gauge.value()).isEqualTo(3.0);

        depth.set(47);
        assertThat(gauge.value())
                .as("a gauge samples on read - that is what makes it a gauge and not a counter")
                .isEqualTo(47.0);
    }

    @Test
    @DisplayName("a Timer keeps count AND distribution, which a mean gauge throws away")
    void timerKeepsTheDistribution() {
        service.handleRequest("/slow", () -> {
            Thread.sleep(30);
            return "ok";
        });
        service.handleRequest("/slow", () -> "ok");

        assertThat(service.requestCount("/slow", "success")).isEqualTo(2);
        assertThat(service.maxLatency("/slow", "success").toMillis())
                .as("""
                        The max survives. Publish a 'mean latency' gauge instead and the tail is
                        gone forever - you cannot recover a percentile from an average.""")
                .isGreaterThanOrEqualTo(25);
    }

    @Test
    @DisplayName("the metrics render in Prometheus exposition format")
    void prometheusScrape() {
        PrometheusMeterRegistry prometheus =
                new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        InstrumentedService instrumented = new InstrumentedService(prometheus);

        instrumented.handleRequest("/orders", () -> "ok");
        instrumented.recordQueueDepth("emails", () -> 7);

        String scrape = prometheus.scrape();
        System.out.println(scrape.lines()
                .filter(line -> line.contains("http_server_requests")
                        || line.contains("worker_queue_depth"))
                .limit(6)
                .reduce("", (a, b) -> a + "  " + b + "\n"));

        assertThat(scrape).contains("http_server_requests");
        assertThat(scrape).contains("endpoint=\"/orders\"");
        assertThat(scrape).contains("outcome=\"success\"");
        assertThat(scrape).contains("worker_queue_depth");
    }

    @Test
    @DisplayName("cardinality: every tag combination is a separate time series")
    void cardinalityMatters() {
        // Ten endpoints, two outcomes -> at most 20 series. Bounded, and cheap.
        for (int i = 0; i < 10; i++) {
            service.handleRequest("/endpoint-" + i, () -> "ok");
        }

        long series = registry.getMeters().stream()
                .filter(meter -> meter.getId().getName().equals(InstrumentedService.REQUEST_TIMER))
                .count();

        System.out.printf("  10 endpoints produced %d time series%n", series);

        assertThat(series)
                .as("""
                        Now imagine tagging by user id instead. Millions of series, one per user -
                        cardinality explosion, and a one-line change that takes down your
                        monitoring system. Tag by bounded values only.""")
                .isEqualTo(10);
    }
}
