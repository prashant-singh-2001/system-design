package sd.p01.day06;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Day 6 - why virtual threads change the shape of server design.
 *
 * <p>A platform thread is an OS thread: ~1 MB of reserved stack, scheduled by the kernel.
 * You can have thousands, not millions. So for blocking work you pool them, and the pool
 * size becomes a hard throughput ceiling - exactly the ceiling Little's Law described
 * on Day 3.
 *
 * <p>A virtual thread is a continuation on the heap, a few hundred bytes, scheduled by the
 * JVM. When it blocks it UNMOUNTS from its carrier thread and the carrier goes and runs
 * something else. Blocking stops being expensive, so you can write straightforward
 * blocking code and still get the concurrency of an async framework.
 *
 * <p>This is the resolution of the "threads vs async" argument that shaped a decade of
 * backend design.
 */
public final class ThreadingLab {

    private ThreadingLab() {
    }

    /**
     * GIVEN - the classic model. {@code poolSize} threads take turns running {@code tasks}
     * blocking tasks, so wall-clock time is roughly {@code tasks / poolSize x taskDuration}.
     */
    public static Duration runOnPlatformThreads(int tasks, int poolSize, Duration taskDuration) {
        CountDownLatch done = new CountDownLatch(tasks);
        long began = System.nanoTime();
        try (ExecutorService pool = Executors.newFixedThreadPool(poolSize)) {
            for (int i = 0; i < tasks; i++) {
                pool.execute(() -> {
                    blockFor(taskDuration);
                    done.countDown();
                });
            }
            await(done);
        }
        return Duration.ofNanos(System.nanoTime() - began);
    }

    /**
     * TODO(day06): run all {@code tasks} tasks, each blocking for {@code taskDuration},
     * on virtual threads. Return the wall-clock time.
     *
     * <p>Use {@code Executors.newVirtualThreadPerTaskExecutor()}. There is no pool size to
     * choose, which is the point: there is nothing to tune and nothing to get wrong.
     * Reuse {@link #blockFor} and {@link #await} exactly as the platform version does, so
     * the comparison is fair.
     *
     * <p>Expect roughly {@code taskDuration} in total rather than
     * {@code tasks / poolSize x taskDuration}. Make sure you can explain why.
     */
    public static Duration runOnVirtualThreads(int tasks, Duration taskDuration) {
        throw new UnsupportedOperationException("TODO(day06): implement runOnVirtualThreads");
    }

    // ---------------------------------------------------------------- given helpers

    /** Stands in for a database call or an HTTP request: blocking, not CPU-bound. */
    static void blockFor(Duration duration) {
        try {
            TimeUnit.NANOSECONDS.sleep(duration.toNanos());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static void await(CountDownLatch latch) {
        try {
            if (!latch.await(2, TimeUnit.MINUTES)) {
                throw new IllegalStateException("tasks did not finish within two minutes");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while waiting", e);
        }
    }
}
