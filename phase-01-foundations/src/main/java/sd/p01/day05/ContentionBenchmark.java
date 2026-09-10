package sd.p01.day05;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/** GIVEN. Hammers a counter from many threads and reports operations per second. */
public final class ContentionBenchmark {

    private ContentionBenchmark() {
    }

    public record Result(String strategy, long finalValue, long elapsedMillis, long opsPerSecond) {
    }

    public static Result run(Supplier<Counter> factory, int threads, int incrementsPerThread) {
        Counter counter = factory.get();

        // Warm up a throwaway instance so we measure compiled code, not the interpreter.
        Counter warmup = factory.get();
        for (int i = 0; i < 50_000; i++) {
            warmup.increment();
        }

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        try (ExecutorService pool = Executors.newFixedThreadPool(threads)) {
            for (int t = 0; t < threads; t++) {
                pool.execute(() -> {
                    try {
                        start.await();
                        for (int i = 0; i < incrementsPerThread; i++) {
                            counter.increment();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            long began = System.nanoTime();
            start.countDown();
            try {
                done.await(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("benchmark interrupted", e);
            }
            long elapsedNanos = System.nanoTime() - began;

            long totalOps = (long) threads * incrementsPerThread;
            long opsPerSecond = elapsedNanos == 0 ? 0 : totalOps * 1_000_000_000L / elapsedNanos;
            return new Result(counter.strategy(), counter.value(),
                    TimeUnit.NANOSECONDS.toMillis(elapsedNanos), opsPerSecond);
        }
    }
}
