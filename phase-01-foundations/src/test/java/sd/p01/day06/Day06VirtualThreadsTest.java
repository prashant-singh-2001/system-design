package sd.p01.day06;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class Day06VirtualThreadsTest {

    private static final int TASKS = 5_000;
    private static final int POOL_SIZE = 50;
    private static final Duration TASK_DURATION = Duration.ofMillis(20);

    @Test
    @DisplayName("5,000 blocking tasks: virtual threads finish in a fraction of the time")
    void virtualThreadsCrushBlockingWorkloads() {
        Duration platform = ThreadingLab.runOnPlatformThreads(TASKS, POOL_SIZE, TASK_DURATION);
        Duration virtual = ThreadingLab.runOnVirtualThreads(TASKS, TASK_DURATION);

        System.out.printf("%n  %,d tasks x %d ms of blocking%n", TASKS, TASK_DURATION.toMillis());
        System.out.printf("  platform threads (pool of %d) : %,5d ms%n", POOL_SIZE, platform.toMillis());
        System.out.printf("  virtual threads               : %,5d ms%n", virtual.toMillis());
        System.out.printf("  speedup                       : %.1fx%n%n",
                (double) platform.toNanos() / Math.max(1, virtual.toNanos()));

        // The pool serialises: 5000 / 50 = 100 rounds x 20 ms is about 2 seconds.
        // Virtual threads all block at once, so total time approaches one task duration.
        assertThat(virtual)
                .as("""
                        Virtual threads should be dramatically faster here because the workload is
                        blocking, not CPU-bound. If the times are similar, check that you used
                        Executors.newVirtualThreadPerTaskExecutor() and not a fixed pool.""")
                .isLessThan(platform.dividedBy(2));
    }

    @Test
    @DisplayName("the platform pool obeys Little's Law - it cannot beat tasks/pool x duration")
    void platformPoolIsBoundedByItsSize() {
        Duration measured = ThreadingLab.runOnPlatformThreads(500, 10, Duration.ofMillis(20));

        // 500 tasks / 10 threads = 50 rounds x 20 ms = 1000 ms floor. Allow scheduler slop.
        assertThat(measured.toMillis())
                .as("a pool of N cannot go faster than tasks/N rounds")
                .isGreaterThanOrEqualTo(900);
    }

    @Test
    @DisplayName("virtual threads are cheap enough to create 100,000 of them")
    void virtualThreadsAreCheapToCreate() {
        Duration elapsed = ThreadingLab.runOnVirtualThreads(100_000, Duration.ofMillis(1));

        System.out.printf("  100,000 virtual threads: %,d ms%n", elapsed.toMillis());

        assertThat(elapsed)
                .as("100,000 platform threads would exhaust memory; virtual threads should not")
                .isLessThan(Duration.ofSeconds(30));
    }
}
