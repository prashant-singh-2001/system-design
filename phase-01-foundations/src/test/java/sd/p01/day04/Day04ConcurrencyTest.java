package sd.p01.day04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class Day04ConcurrencyTest {

    private static final int THREADS = 8;
    private static final int INCREMENTS_PER_THREAD = 50_000;
    private static final long EXPECTED = (long) THREADS * INCREMENTS_PER_THREAD;

    private static long hammer(Counter counter) throws InterruptedException {
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);
        try (ExecutorService pool = Executors.newFixedThreadPool(THREADS)) {
            for (int t = 0; t < THREADS; t++) {
                pool.execute(() -> {
                    try {
                        start.await();
                        for (int i = 0; i < INCREMENTS_PER_THREAD; i++) {
                            counter.increment();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }
            start.countDown();          // release everyone at once, to maximise overlap
            done.await(30, TimeUnit.SECONDS);
        }
        return counter.value();
    }

    @Test
    @DisplayName("SafeCounter loses nothing: exactly 400,000 increments")
    void safeCounterIsExact() throws InterruptedException {
        assertThat(hammer(new SafeCounter()))
                .as("every increment must be accounted for, on every run, on every machine")
                .isEqualTo(EXPECTED);
    }

    @Test
    @DisplayName("UnsafeCounter can only lose increments, never invent them")
    void unsafeCounterLosesUpdates() throws InterruptedException {
        long actual = hammer(new UnsafeCounter());

        System.out.printf("  expected %,d  got %,d  (lost %,d)%n",
                EXPECTED, actual, EXPECTED - actual);

        // Asserting that it DOES lose updates would be flaky - it might get lucky.
        // Asserting it never exceeds the true count is always true, and is the point:
        // a lost update is silent. Nothing throws. The number is just quietly wrong.
        assertThat(actual).isLessThanOrEqualTo(EXPECTED);
    }

    @Test
    @DisplayName("a spinning reader must observe the stop signal")
    void stopSignalIsVisibleAcrossThreads() {
        StopSignal signal = new StopSignal();

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Thread spinner = new Thread(() -> {
                while (!signal.shouldStop()) {
                    // deliberately empty: no synchronisation, no I/O, nothing that
                    // would accidentally create a memory barrier for us
                }
            }, "spinner");
            // Daemon, so a still-spinning thread can never hold the JVM open when this
            // assertion times out - which is exactly what happens before you fix the field.
            spinner.setDaemon(true);
            spinner.start();

            Thread.sleep(200);
            signal.stop();
            spinner.join();
        }, """
                The spinner never saw stop(). Without a happens-before edge the JIT may hoist
                the field read out of the loop. One keyword on the field fixes it.""");
    }
}
