package sd.p08.day72;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Random;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day72RetryTest {

    private static final Duration BASE = Duration.ofMillis(100);
    private static final Duration MAX = Duration.ofSeconds(10);

    @Test
    @DisplayName("fixed backoff never changes")
    void fixedBackoff() {
        for (int attempt = 1; attempt <= 5; attempt++) {
            assertThat(BackoffStrategy.fixed(BASE, attempt)).isEqualTo(BASE);
        }
    }

    @Test
    @DisplayName("exponential doubles, and respects the cap")
    void exponentialBackoff() {
        assertThat(BackoffStrategy.exponential(BASE, MAX, 1)).isEqualTo(Duration.ofMillis(100));
        assertThat(BackoffStrategy.exponential(BASE, MAX, 2)).isEqualTo(Duration.ofMillis(200));
        assertThat(BackoffStrategy.exponential(BASE, MAX, 3)).isEqualTo(Duration.ofMillis(400));
        assertThat(BackoffStrategy.exponential(BASE, MAX, 4)).isEqualTo(Duration.ofMillis(800));

        assertThat(BackoffStrategy.exponential(BASE, MAX, 20))
                .as("without a cap this would be about 13 hours")
                .isEqualTo(MAX);
    }

    @Test
    @DisplayName("full jitter stays within the exponential envelope")
    void fullJitterBounds() {
        Random random = new Random(42);

        for (int attempt = 1; attempt <= 8; attempt++) {
            Duration ceiling = BackoffStrategy.exponential(BASE, MAX, attempt);
            for (int i = 0; i < 200; i++) {
                assertThat(BackoffStrategy.fullJitter(BASE, MAX, attempt, random))
                        .isBetween(Duration.ZERO, ceiling);
            }
        }
    }

    @Test
    @DisplayName("full jitter actually varies - a constant would defeat the point")
    void fullJitterVaries() {
        Random random = new Random(42);

        long distinct = IntStream.range(0, 200)
                .mapToObj(i -> BackoffStrategy.fullJitter(BASE, MAX, 5, random))
                .distinct()
                .count();

        assertThat(distinct).isGreaterThan(50);
    }

    @Test
    @DisplayName("backoff rejects nonsense")
    void backoffValidation() {
        assertThatThrownBy(() -> BackoffStrategy.exponential(BASE, MAX, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BackoffStrategy.exponential(Duration.ZERO, MAX, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BackoffStrategy.exponential(
                Duration.ofSeconds(5), Duration.ofSeconds(1), 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("THE measurement: jitter flattens the retry storm")
    void jitterFlattensTheStorm() {
        int clients = 5_000;
        int attempts = 5;

        int exponentialPeak = RetryStormSimulator.peakConcurrentRetries(
                clients, attempts, BASE, MAX,
                RetryStormSimulator.Strategy.EXPONENTIAL, new Random(42));
        int jitteredPeak = RetryStormSimulator.peakConcurrentRetries(
                clients, attempts, BASE, MAX,
                RetryStormSimulator.Strategy.FULL_JITTER, new Random(42));

        System.out.printf("%n  %,d clients x %d attempts%n", clients, attempts);
        System.out.printf("    exponential : peak %,d retries in one 100 ms slot%n", exponentialPeak);
        System.out.printf("    full jitter : peak %,d retries in one 100 ms slot%n%n", jitteredPeak);

        assertThat(jitteredPeak)
                .as("""
                        Same total retries, same average rate, wildly different peak. The peak is
                        what decides whether the struggling dependency recovers or falls over, and
                        it is invisible if you only count total retries.""")
                .isLessThan(exponentialPeak);
    }

    @Test
    @DisplayName("a retry budget earns tokens from successes")
    void budgetEarnsFromSuccess() {
        RetryBudget budget = new RetryBudget(0.1, 100);

        for (int i = 0; i < 100; i++) {
            budget.recordSuccess();
        }

        assertThat(budget.availableTokens())
                .as("100 successes at a 10%% ratio buys 10 retries")
                .isEqualTo(10.0, Offset.offset(1e-9));
    }

    @Test
    @DisplayName("the budget is capped, so a quiet period cannot bank unlimited retries")
    void budgetIsCapped() {
        RetryBudget budget = new RetryBudget(0.5, 10);

        for (int i = 0; i < 1_000; i++) {
            budget.recordSuccess();
        }

        assertThat(budget.availableTokens()).isEqualTo(10.0, Offset.offset(1e-9));
    }

    @Test
    @DisplayName("THE inversion: when everything is failing, the budget stops the retries")
    void budgetEmptiesWhenItMattersMost() {
        RetryBudget budget = new RetryBudget(0.1, 100);

        for (int i = 0; i < 50; i++) {
            budget.recordSuccess();
        }
        assertThat(budget.availableTokens()).isEqualTo(5.0, Offset.offset(1e-9));

        // The dependency now fails for everyone. No successes, so no new budget.
        int allowed = 0;
        for (int i = 0; i < 100; i++) {
            if (budget.tryConsume()) {
                allowed++;
            }
        }

        System.out.printf("  100 failures wanted a retry; the budget allowed %d%n", allowed);

        assertThat(allowed)
                .as("""
                        The budget is most generous when you least need it and most restrictive
                        when retrying would hurt - the exact opposite of naive retry logic.""")
                .isEqualTo(5);
        assertThat(budget.rejected()).isEqualTo(95);
    }

    @Test
    @DisplayName("a budget rejects an impossible configuration")
    void budgetValidation() {
        assertThatThrownBy(() -> new RetryBudget(0, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RetryBudget(1.5, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RetryBudget(0.1, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
