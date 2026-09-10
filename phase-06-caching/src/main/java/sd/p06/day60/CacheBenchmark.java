package sd.p06.day60;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.random.RandomGenerator;

/**
 * TODO(day60): the phase review. Prove the cache was worth it, with numbers.
 *
 * <p>Run the same workload twice - once straight to the slow source, once through a cache - and
 * report the percentile distributions. This is the shape of every performance claim you should
 * ever make: same workload, two configurations, percentiles not averages.
 *
 * <p>{@code runUncached}: for each request, pay {@code sourceLatency} and record it.
 *
 * <p>{@code runCached}: keep a {@code Map} cache. On a hit, record {@code cacheLatency}. On a
 * miss, pay {@code sourceLatency}, store the value, and record the miss latency. Return a
 * {@link BenchmarkResult} carrying the recorder and the hit ratio.
 *
 * <p>Use {@link #zipfianKey} so the workload is realistically skewed. That matters enormously:
 * a uniform random workload over a large key space makes any cache look useless, and real
 * traffic is never uniform. Choosing a representative workload is most of the work in
 * benchmarking, and the most common place benchmarks quietly lie.
 *
 * <p>Watch what happens to the numbers: p50 collapses because most requests hit, while p99 stays
 * near the source latency because the misses still have to pay. <b>A cache improves the median
 * far more than the tail.</b> If you need the tail as well, you need the source itself to be
 * fast - or Day 54's early recompute so a user never waits on a refill.
 */
public final class CacheBenchmark {

    private CacheBenchmark() {
    }

    public record BenchmarkResult(LatencyRecorder latencies, double hitRatio) {
    }

    public static LatencyRecorder runUncached(int requests, int keySpace,
                                              Duration sourceLatency, RandomGenerator random) {
        throw new UnsupportedOperationException("TODO(day60): every request pays full price");
    }

    public static BenchmarkResult runCached(int requests, int keySpace, int cacheCapacity,
                                            Duration sourceLatency, Duration cacheLatency,
                                            RandomGenerator random) {
        throw new UnsupportedOperationException("TODO(day60): hit or miss, record, report");
    }

    // ---------------------------------------------------------------- given

    /**
     * A Zipf-like key: small indexes are far more likely than large ones, which is how real
     * traffic actually behaves. Squaring a uniform sample is a crude but effective skew.
     */
    public static String zipfianKey(int keySpace, RandomGenerator random) {
        double uniform = random.nextDouble();
        int index = (int) (uniform * uniform * keySpace);
        return "key-" + Math.min(index, keySpace - 1);
    }

    /** A trivial bounded map cache; eviction is not today's subject. */
    public static Map<String, String> newCache() {
        return new HashMap<>();
    }
}
