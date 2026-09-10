package sd.p08.day73;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day73BulkheadTest {

    @Test
    @DisplayName("calls within the limit pass through")
    void withinLimit() {
        Bulkhead bulkhead = new Bulkhead("payments", 5);

        for (int i = 0; i < 100; i++) {
            assertThat(bulkhead.execute(() -> "ok")).isEqualTo("ok");
        }

        assertThat(bulkhead.rejected()).isZero();
    }

    @Test
    @DisplayName("permits are released even when the action throws")
    void permitsAreReleasedOnFailure() {
        Bulkhead bulkhead = new Bulkhead("flaky", 1);

        for (int i = 0; i < 10; i++) {
            assertThatThrownBy(() -> bulkhead.execute(() -> {
                throw new IllegalStateException("boom");
            })).isInstanceOf(IllegalStateException.class);
        }

        assertThat(bulkhead.execute(() -> "ok"))
                .as("""
                        A leaked permit is a slow-motion outage that only shows up under load.
                        Release in a finally block, always.""")
                .isEqualTo("ok");
    }

    @Test
    @DisplayName("concurrency is capped at the permit count")
    void concurrencyIsBounded() throws Exception {
        int permits = 4;
        int callers = 40;
        Bulkhead bulkhead = new Bulkhead("slow-api", permits);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger rejected = new AtomicInteger();

        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Callable<String>> calls = new ArrayList<>();
            for (int i = 0; i < callers; i++) {
                calls.add(() -> {
                    try {
                        return bulkhead.execute(() -> {
                            release.await(5, java.util.concurrent.TimeUnit.SECONDS);
                            return "ok";
                        });
                    } catch (Bulkhead.BulkheadFullException e) {
                        rejected.incrementAndGet();
                        return "rejected";
                    }
                });
            }

            // Start them all, let them pile up against the permit limit, then release.
            List<Future<String>> running = new ArrayList<>();
            for (Callable<String> call : calls) {
                running.add(pool.submit(call));
            }
            Thread.sleep(300);
            release.countDown();
            for (Future<String> future : running) {
                future.get();
            }
        }

        System.out.printf("  %d callers, %d permits -> peak in flight %d, rejected %d%n",
                callers, permits, bulkhead.peakInFlight(), bulkhead.rejected());

        assertThat(bulkhead.peakInFlight())
                .as("the limit is the promise; exceeding it means the permit accounting is wrong")
                .isLessThanOrEqualTo(permits);
        assertThat(bulkhead.rejected()).isGreaterThan(0);
    }

    @Test
    @DisplayName("THE point: a saturated dependency cannot starve an unrelated one")
    void isolationBetweenDependencies() throws Exception {
        Bulkhead recommendations = new Bulkhead("recommendations", 2);
        Bulkhead payments = new Bulkhead("payments", 2);
        CountDownLatch stuck = new CountDownLatch(1);

        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            // Recommendations has gone slow. Fill its whole budget with stuck calls.
            for (int i = 0; i < 10; i++) {
                pool.submit(() -> {
                    try {
                        return recommendations.execute(() -> {
                            stuck.await(5, java.util.concurrent.TimeUnit.SECONDS);
                            return "slow";
                        });
                    } catch (Bulkhead.BulkheadFullException e) {
                        return "rejected";
                    }
                });
            }
            Thread.sleep(300);

            assertThat(payments.execute(() -> "charged"))
                    .as("""
                            Share one pool and a slow recommendation API takes payments down with
                            it - not by failing, just by being slow. Separate budgets mean you
                            chose in advance which feature degrades under pressure.""")
                    .isEqualTo("charged");

            stuck.countDown();
        }

        assertThat(recommendations.rejected()).isGreaterThan(0);
        assertThat(payments.rejected()).isZero();
    }

    @Test
    @DisplayName("a bulkhead needs at least one permit")
    void validation() {
        assertThatThrownBy(() -> new Bulkhead("x", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
