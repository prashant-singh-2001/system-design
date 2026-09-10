package sd.p08.day80;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sd.p08.day72.RetryBudget;
import sd.p08.day73.Bulkhead;
import sd.p08.day73.CircuitBreaker;
import sd.p08.day79.ErrorBudget;
import sd.p08.day79.Slo;
import sd.p08.support.MutableClock;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class Day80HardeningTest {

    private MutableClock clock;

    private HardenedClient client(double failureRate, FlakyDependency[] out) {
        clock = MutableClock.startingAt("2026-03-01T12:00:00Z");
        FlakyDependency dependency = new FlakyDependency(failureRate, new Random(7));
        if (out != null) {
            out[0] = dependency;
        }
        return new HardenedClient(
                dependency,
                new Bulkhead("dependency", 10),
                new CircuitBreaker(5, Duration.ofSeconds(30), 2, clock),
                new RetryBudget(0.2, 100),
                3,
                new Random(11));
    }

    @Test
    @DisplayName("a healthy dependency is called once per request")
    void happyPath() {
        FlakyDependency[] out = new FlakyDependency[1];
        HardenedClient client = client(0.0, out);

        for (int i = 0; i < 100; i++) {
            assertThat(client.call("req-" + i)).contains("handled:req-" + i);
        }

        assertThat(client.successes()).isEqualTo(100);
        assertThat(out[0].calls())
                .as("no retries needed, so no extra load")
                .isEqualTo(100);
        assertThat(client.degradedResponses()).isZero();
    }

    @Test
    @DisplayName("transient failures are absorbed by retries")
    void retriesAbsorbTransientFailures() {
        FlakyDependency[] out = new FlakyDependency[1];
        HardenedClient client = client(0.3, out);

        for (int i = 0; i < 200; i++) {
            client.call("req-" + i);
        }

        System.out.printf("  30%% failure rate -> %d succeeded, %d degraded, %d dependency calls%n",
                client.successes(), client.degradedResponses(), out[0].calls());

        assertThat(client.successes())
                .as("retrying a 30%% failure rate three times should recover most requests")
                .isGreaterThan(150);
    }

    @Test
    @DisplayName("a total outage trips the breaker, and load on the dependency collapses")
    void breakerProtectsADeadDependency() {
        FlakyDependency[] out = new FlakyDependency[1];
        HardenedClient client = client(0.0, out);

        client.call("warmup");
        out[0].goDown();

        for (int i = 0; i < 200; i++) {
            assertThat(client.call("req-" + i)).isEmpty();
        }

        System.out.printf("  200 requests against a dead dependency -> %d actual calls%n",
                out[0].calls());

        assertThat(out[0].calls())
                .as("""
                        Once the breaker trips, requests fail in microseconds without touching the
                        dependency. Without it, 200 requests would each burn attempts and threads
                        against something already down.""")
                .isLessThan(60);
        assertThat(client.degradedResponses()).isEqualTo(200);
    }

    @Test
    @DisplayName("failures return empty rather than throwing - the caller can degrade")
    void degradesGracefully() {
        FlakyDependency[] out = new FlakyDependency[1];
        HardenedClient client = client(0.0, out);
        out[0].goDown();

        Optional<String> result = client.call("req");

        assertThat(result)
                .as("""
                        A bounded, fast, predictable failure the caller can plan for - a cached
                        value, a default, a partial page. That is the point of the whole stack.""")
                .isEmpty();
    }

    @Test
    @DisplayName("the breaker recovers once the dependency does")
    void recoversAfterTheOutage() {
        FlakyDependency[] out = new FlakyDependency[1];
        HardenedClient client = client(0.0, out);

        out[0].goDown();
        for (int i = 0; i < 20; i++) {
            client.call("req-" + i);
        }

        out[0].recover();
        clock.advance(Duration.ofSeconds(31));

        client.call("probe-1");
        client.call("probe-2");

        assertThat(client.call("after-recovery"))
                .as("the half-open probes closed the breaker and traffic resumed")
                .contains("handled:after-recovery");
    }

    @Test
    @DisplayName("THE phase result: the hardened client holds its SLO through a partial outage")
    void survivesChaosWithinTheErrorBudget() {
        FlakyDependency[] out = new FlakyDependency[1];
        HardenedClient client = client(0.1, out);
        Slo slo = Slo.thirtyDay("checkout", 0.99);

        int requests = 500;
        for (int i = 0; i < requests; i++) {
            client.call("req-" + i);
        }

        double availability = (double) client.successes() / requests;
        double burn = ErrorBudget.burnRate(requests, client.degradedResponses(), slo.target());

        System.out.printf("%n  10%% dependency failure rate, %d requests%n", requests);
        System.out.printf("    observed availability : %.2f%%%n", availability * 100);
        System.out.printf("    SLO target            : %.2f%%%n", slo.target() * 100);
        System.out.printf("    error budget burn rate: %.2fx%n%n", burn);

        assertThat(availability)
                .as("""
                        A dependency failing one call in ten, and the service still meets a 99%%
                        SLO. Nothing here is clever on its own - retries with jitter, a breaker, a
                        bulkhead, a budget. Composed, and in the right order, they turn a
                        dependency's bad day into a number you can defend.""")
                .isGreaterThanOrEqualTo(slo.target());
    }
}
