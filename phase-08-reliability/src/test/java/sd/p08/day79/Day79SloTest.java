package sd.p08.day79;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day79SloTest {

    private static final Offset<Double> TOLERANCE = Offset.offset(1e-6);

    @Test
    @DisplayName("each extra nine costs a tenth of the downtime")
    void allowedDowntime() {
        assertThat(Slo.thirtyDay("api", 0.99).allowedDowntime().toMinutes())
                .as("about 7 hours 12 minutes a month")
                .isEqualTo(432);
        assertThat(Slo.thirtyDay("api", 0.999).allowedDowntime().toMinutes())
                .as("about 43 minutes")
                .isEqualTo(43);
        assertThat(Slo.thirtyDay("api", 0.9999).allowedDowntime().toSeconds())
                .as("about 4 minutes 19 seconds")
                .isBetween(255L, 265L);
        assertThat(Slo.thirtyDay("api", 0.99999).allowedDowntime().toSeconds())
                .as("""
                        Twenty-six seconds a month - less than a single deploy, or one bad DNS
                        change. Putting this number next to the request is what makes the
                        conversation honest.""")
                .isBetween(20L, 30L);
    }

    @Test
    @DisplayName("an impossible SLO is rejected")
    void sloValidation() {
        assertThatThrownBy(() -> new Slo("api", 1.0, Duration.ofDays(30)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Slo("api", 0.0, Duration.ofDays(30)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Slo("api", 0.999, Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("the budget is failures you are permitted, not failures to avoid")
    void allowedFailures() {
        assertThat(ErrorBudget.allowedFailures(1_000_000, 0.999))
                .as("a million requests at 99.9%% permits a thousand failures")
                .isEqualTo(1_000.0, TOLERANCE);
        assertThat(ErrorBudget.allowedFailures(1_000_000, 0.9999)).isEqualTo(100.0, TOLERANCE);
    }

    @Test
    @DisplayName("consumption and remainder")
    void consumption() {
        assertThat(ErrorBudget.consumedFraction(1_000_000, 250, 0.999))
                .as("250 of a permitted 1000")
                .isEqualTo(0.25, TOLERANCE);
        assertThat(ErrorBudget.remainingFraction(1_000_000, 250, 0.999))
                .isEqualTo(0.75, TOLERANCE);
    }

    @Test
    @DisplayName("an exhausted budget stops at zero rather than going negative")
    void exhaustedBudget() {
        assertThat(ErrorBudget.consumedFraction(1_000_000, 3_000, 0.999))
                .as("three times over - the SLO is breached")
                .isEqualTo(3.0, TOLERANCE);
        assertThat(ErrorBudget.remainingFraction(1_000_000, 3_000, 0.999)).isZero();
    }

    @Test
    @DisplayName("no traffic consumes no budget")
    void noTraffic() {
        assertThat(ErrorBudget.consumedFraction(0, 0, 0.999)).isZero();
        assertThat(ErrorBudget.remainingFraction(0, 0, 0.999)).isEqualTo(1.0, TOLERANCE);
    }

    @Test
    @DisplayName("THE alerting signal: burn rate, not raw error count")
    void burnRate() {
        assertThat(ErrorBudget.burnRate(100_000, 100, 0.999))
                .as("0.1%% errors against a 99.9%% SLO is exactly sustainable")
                .isEqualTo(1.0, TOLERANCE);

        assertThat(ErrorBudget.burnRate(100_000, 1_000, 0.99))
                .as("1%% errors at a 99%% SLO is exactly on budget - not an incident")
                .isEqualTo(1.0, TOLERANCE);
        assertThat(ErrorBudget.burnRate(100_000, 1_000, 0.9999))
                .as("""
                        The same 1%% error rate against 99.99%% burns a month of budget in about
                        seven hours. This is why you alert on burn rate: the raw error count means
                        nothing without the target beside it.""")
                .isEqualTo(100.0, TOLERANCE);
    }

    @Test
    @DisplayName("time to exhaustion turns a burn rate into a page-or-not decision")
    void timeToExhaustion() {
        Duration window = Duration.ofDays(30);

        assertThat(ErrorBudget.timeToExhaustion(1.0, 1.0, window))
                .as("burning exactly on budget lasts the whole window")
                .isEqualTo(window);
        assertThat(ErrorBudget.timeToExhaustion(1.0, 10.0, window).toDays())
                .as("ten times too fast burns a 30-day budget in three")
                .isEqualTo(3);
        assertThat(ErrorBudget.timeToExhaustion(0.0, 5.0, window))
                .as("already gone")
                .isEqualTo(Duration.ZERO);
        assertThat(ErrorBudget.timeToExhaustion(0.5, 0.0, window))
                .as("not burning at all - no exhaustion to predict")
                .isNull();
    }

    @Test
    @DisplayName("a chaos experiment states a hypothesis and measures against the SLO")
    void chaosWithinBudget() {
        Slo slo = Slo.thirtyDay("api", 0.99);

        ChaosExperiment.ExperimentResult result = ChaosExperiment.run(
                10_000, 0.005, () -> true, new java.util.Random(42));

        System.out.printf("  0.5%% injected failure -> availability %.3f%%%n",
                result.observedAvailability() * 100);

        assertThat(result.requests()).isEqualTo(10_000);
        assertThat(result.succeeded() + result.failed()).isEqualTo(10_000);
        assertThat(result.holdsHypothesis(slo))
                .as("the hypothesis held: the system absorbed the injected failure")
                .isTrue();
    }

    @Test
    @DisplayName("an experiment that breaches the SLO has disproved the hypothesis - abort")
    void chaosBeyondBudget() {
        Slo slo = Slo.thirtyDay("api", 0.999);

        ChaosExperiment.ExperimentResult result = ChaosExperiment.run(
                10_000, 0.05, () -> true, new java.util.Random(42));

        System.out.printf("  5%% injected failure -> availability %.3f%%%n",
                result.observedAvailability() * 100);

        assertThat(result.holdsHypothesis(slo))
                .as("""
                        This is a successful experiment, not a failed one: you found the limit on a
                        Tuesday afternoon rather than at 3am. Abort, and go fix it.""")
                .isFalse();
    }

    @Test
    @DisplayName("failures raised by the operation itself count too")
    void operationFailuresCount() {
        ChaosExperiment.ExperimentResult result = ChaosExperiment.run(
                1_000, 0.0, () -> false, new java.util.Random(1));

        assertThat(result.failed())
                .as("no injected failures, but the operation reported failure every time")
                .isEqualTo(1_000);
        assertThat(result.observedAvailability()).isZero();
    }
}
