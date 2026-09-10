package sd.p06.day54;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day54StampedeTest {

    private static final int CONCURRENT_READERS = 200;
    private static final Duration LOAD_COST = Duration.ofMillis(50);

    private <T> List<T> allAtOnce(int count, Callable<T> task) throws Exception {
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Callable<T>> calls = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                calls.add(task);
            }
            List<T> results = new ArrayList<>();
            for (Future<T> future : pool.invokeAll(calls)) {
                results.add(future.get());
            }
            return results;
        }
    }

    @Test
    @DisplayName("the naive cache stampedes: one cold key, hundreds of loads")
    void naiveCacheStampedes() throws Exception {
        ExpensiveSource source = new ExpensiveSource(LOAD_COST);
        NaiveCache cache = new NaiveCache(source);

        allAtOnce(CONCURRENT_READERS, () -> cache.get("hot-key"));

        System.out.printf("  naive        : %,d readers -> %,d source calls%n",
                CONCURRENT_READERS, source.calls());

        assertThat(source.calls())
                .as("every concurrent misser called the source - this is the herd")
                .isGreaterThan(1);
    }

    @Test
    @DisplayName("single-flight: hundreds of readers, exactly ONE load")
    void singleFlightCollapsesTheHerd() throws Exception {
        ExpensiveSource source = new ExpensiveSource(LOAD_COST);
        SingleFlightCache cache = new SingleFlightCache(source);

        List<String> results = allAtOnce(CONCURRENT_READERS, () -> cache.get("hot-key"));

        System.out.printf("  single-flight: %,d readers -> %,d source call%n",
                CONCURRENT_READERS, source.calls());

        assertThat(source.calls())
                .as("one loader per key; everyone else waits for its result")
                .isEqualTo(1);
        assertThat(results)
                .as("and every reader still gets the right answer")
                .containsOnly("value-for-hot-key");
    }

    @Test
    @DisplayName("single-flight still loads different keys in parallel")
    void differentKeysAreNotSerialized() throws Exception {
        ExpensiveSource source = new ExpensiveSource(LOAD_COST);
        SingleFlightCache cache = new SingleFlightCache(source);

        long began = System.nanoTime();
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Callable<String>> calls = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                String key = "key-" + i;
                calls.add(() -> cache.get(key));
            }
            pool.invokeAll(calls);
        }
        Duration elapsed = Duration.ofNanos(System.nanoTime() - began);

        assertThat(source.calls()).isEqualTo(20);
        assertThat(elapsed)
                .as("20 distinct keys at 50 ms each must not serialize into a second")
                .isLessThan(Duration.ofMillis(700));
    }

    @Test
    @DisplayName("jitter spreads deadlines across a window instead of one instant")
    void jitterSpreadsExpiry() {
        Random random = new Random(42);
        Duration base = Duration.ofMinutes(60);

        List<Duration> ttls = new ArrayList<>();
        for (int i = 0; i < 1_000; i++) {
            ttls.add(JitteredTtl.jitter(base, 0.2, random));
        }

        Duration min = ttls.stream().min(Duration::compareTo).orElseThrow();
        Duration max = ttls.stream().max(Duration::compareTo).orElseThrow();
        System.out.printf("  jittered TTL : %d..%d minutes around a %d minute base%n",
                min.toMinutes(), max.toMinutes(), base.toMinutes());

        assertThat(min).isGreaterThanOrEqualTo(Duration.ofMinutes(48));
        assertThat(max).isLessThanOrEqualTo(Duration.ofMinutes(72));
        assertThat(max.minus(min))
                .as("the whole point is that they do not all land together")
                .isGreaterThan(Duration.ofMinutes(15));
    }

    @Test
    @DisplayName("zero jitter is just the base TTL")
    void zeroJitter() {
        assertThat(JitteredTtl.jitter(Duration.ofMinutes(60), 0, new Random(1)))
                .isEqualTo(Duration.ofMinutes(60));
    }

    @Test
    @DisplayName("jitter rejects nonsense")
    void jitterValidation() {
        Random random = new Random(1);
        assertThatThrownBy(() -> JitteredTtl.jitter(Duration.ofMinutes(60), -0.1, random))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> JitteredTtl.jitter(Duration.ofMinutes(60), 1.5, random))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> JitteredTtl.jitter(Duration.ZERO, 0.2, random))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("early recompute never fires in the entry's first 80%")
    void noEarlyRefreshWhileFresh() {
        Random random = new Random(7);
        Duration ttl = Duration.ofMinutes(100);

        for (int minute = 0; minute < 80; minute++) {
            assertThat(EarlyRecompute.shouldRefreshEarly(
                    Duration.ofMinutes(minute), ttl, 0.8, random))
                    .as("at %d%% of its life, beta=0.8 means never", minute)
                    .isFalse();
        }
    }

    @Test
    @DisplayName("early recompute ramps up over the final stretch")
    void refreshProbabilityRamps() {
        Random random = new Random(7);
        Duration ttl = Duration.ofMinutes(100);

        long at85 = count(random, ttl, Duration.ofMinutes(85));
        long at95 = count(random, ttl, Duration.ofMinutes(95));

        System.out.printf("  early refresh: %d%% at 85%% of TTL, %d%% at 95%%%n", at85 / 10, at95 / 10);

        assertThat(at85).as("some, but not most").isBetween(100L, 500L);
        assertThat(at95).as("most, but the ramp is still linear").isGreaterThan(at85);
    }

    @Test
    @DisplayName("an entry at or past its TTL always refreshes")
    void expiredAlwaysRefreshes() {
        Random random = new Random(7);
        Duration ttl = Duration.ofMinutes(100);

        assertThat(EarlyRecompute.shouldRefreshEarly(ttl, ttl, 0.8, random)).isTrue();
        assertThat(EarlyRecompute.shouldRefreshEarly(
                Duration.ofMinutes(200), ttl, 0.8, random)).isTrue();
    }

    private long count(Random random, Duration ttl, Duration age) {
        long refreshes = 0;
        for (int i = 0; i < 1_000; i++) {
            if (EarlyRecompute.shouldRefreshEarly(age, ttl, 0.8, random)) {
                refreshes++;
            }
        }
        return refreshes;
    }
}
