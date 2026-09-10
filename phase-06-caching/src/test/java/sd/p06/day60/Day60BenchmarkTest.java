package sd.p06.day60;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day60BenchmarkTest {

    @Test
    @DisplayName("percentiles use nearest-rank, so p50 of 1..100 is 50")
    void percentiles() {
        LatencyRecorder recorder = new LatencyRecorder();
        for (int i = 1; i <= 100; i++) {
            recorder.record(Duration.ofMillis(i));
        }

        assertThat(recorder.percentile(50)).isEqualTo(Duration.ofMillis(50));
        assertThat(recorder.percentile(95)).isEqualTo(Duration.ofMillis(95));
        assertThat(recorder.percentile(99)).isEqualTo(Duration.ofMillis(99));
        assertThat(recorder.percentile(100)).isEqualTo(Duration.ofMillis(100));
        assertThat(recorder.count()).isEqualTo(100);
    }

    @Test
    @DisplayName("THE reason percentiles exist: the mean hides the tail")
    void meanHidesTheTail() {
        LatencyRecorder recorder = new LatencyRecorder();
        for (int i = 0; i < 99; i++) {
            recorder.record(Duration.ofMillis(1));
        }
        recorder.record(Duration.ofMillis(1_000));

        System.out.println("  " + recorder.summary("skewed"));

        assertThat(recorder.mean().toMillis())
                .as("the mean describes nobody's actual experience")
                .isBetween(9L, 12L);
        assertThat(recorder.percentile(50)).isEqualTo(Duration.ofMillis(1));
        assertThat(recorder.percentile(100)).isEqualTo(Duration.ofMillis(1_000));
    }

    @Test
    @DisplayName("an empty recorder returns zero rather than throwing")
    void emptyRecorder() {
        LatencyRecorder recorder = new LatencyRecorder();

        assertThat(recorder.mean()).isEqualTo(Duration.ZERO);
        assertThat(recorder.percentile(99)).isEqualTo(Duration.ZERO);
        assertThat(recorder.count()).isZero();
    }

    @Test
    @DisplayName("a single sample is every percentile")
    void singleSample() {
        LatencyRecorder recorder = new LatencyRecorder();
        recorder.record(Duration.ofMillis(7));

        assertThat(recorder.percentile(1)).isEqualTo(Duration.ofMillis(7));
        assertThat(recorder.percentile(99)).isEqualTo(Duration.ofMillis(7));
    }

    @Test
    @DisplayName("percentiles outside (0, 100] are rejected")
    void percentileValidation() {
        LatencyRecorder recorder = new LatencyRecorder();
        recorder.record(Duration.ofMillis(1));

        assertThatThrownBy(() -> recorder.percentile(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> recorder.percentile(101))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> recorder.percentile(-5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("a skewed workload gets a high hit ratio from a small cache")
    void skewMakesCachingWork() {
        CacheBenchmark.BenchmarkResult result = CacheBenchmark.runCached(
                20_000, 10_000, 500,
                Duration.ofMillis(1), Duration.ofNanos(100), new Random(42));

        System.out.printf("%n  hit ratio with a 500-entry cache over 10,000 keys: %.1f%%%n",
                result.hitRatio() * 100);

        assertThat(result.hitRatio())
                .as("""
                        Real traffic is Zipfian, so a cache holding 5%% of the key space still
                        catches most requests. Benchmark with uniform random keys and you will
                        conclude, wrongly, that caching does not work.""")
                .isGreaterThan(0.5);
    }

    @Test
    @DisplayName("THE phase result: caching collapses p50 far more than p99")
    void cachingHelpsTheMedianMostAndTheTailLeast() {
        Duration source = Duration.ofMillis(1);
        Duration cached = Duration.ofNanos(100);

        LatencyRecorder uncached =
                CacheBenchmark.runUncached(20_000, 10_000, source, new Random(42));
        CacheBenchmark.BenchmarkResult withCache = CacheBenchmark.runCached(
                20_000, 10_000, 500, source, cached, new Random(42));

        System.out.println();
        System.out.println("  " + uncached.summary("no cache"));
        System.out.println("  " + withCache.latencies().summary("with cache"));
        System.out.printf("  hit ratio: %.1f%%%n%n", withCache.hitRatio() * 100);

        assertThat(withCache.latencies().percentile(50))
                .as("most requests hit, so the median collapses")
                .isLessThan(uncached.percentile(50));

        assertThat(withCache.latencies().percentile(99))
                .as("""
                        Misses still pay full price, so the tail barely moves. This is why a cache
                        is not a fix for a slow dependency - it is a fix for a BUSY one.""")
                .isGreaterThanOrEqualTo(source.dividedBy(2));
    }

    @Test
    @DisplayName("the uncached run pays full price every single time")
    void uncachedIsFlat() {
        LatencyRecorder recorder = CacheBenchmark.runUncached(
                1_000, 100, Duration.ofMillis(2), new Random(1));

        assertThat(recorder.count()).isEqualTo(1_000);
        assertThat(recorder.percentile(50)).isEqualTo(Duration.ofMillis(2));
        assertThat(recorder.percentile(99)).isEqualTo(Duration.ofMillis(2));
    }
}
