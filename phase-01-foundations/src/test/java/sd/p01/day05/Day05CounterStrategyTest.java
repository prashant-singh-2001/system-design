package sd.p01.day05;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class Day05CounterStrategyTest {

    private static final int THREADS = 8;
    private static final int PER_THREAD = 200_000;
    private static final long EXPECTED = (long) THREADS * PER_THREAD;

    static Stream<Supplier<Counter>> allStrategies() {
        return Stream.of(
                SynchronizedCounter::new,
                LockCounter::new,
                AtomicCounter::new,
                LongAdderCounter::new);
    }

    @ParameterizedTest(name = "{index}: every strategy must be exact")
    @MethodSource("allStrategies")
    void everyStrategyCountsExactly(Supplier<Counter> factory) {
        ContentionBenchmark.Result result = ContentionBenchmark.run(factory, THREADS, PER_THREAD);

        assertThat(result.finalValue())
                .as("%s lost or duplicated increments", result.strategy())
                .isEqualTo(EXPECTED);
    }

    @Test
    @DisplayName("compare them - the ordering is the lesson, not the absolute numbers")
    void compareStrategies() {
        List<ContentionBenchmark.Result> results = allStrategies()
                .map(factory -> ContentionBenchmark.run(factory, THREADS, PER_THREAD))
                .toList();

        System.out.printf("%n  %-16s %12s %10s%n", "strategy", "ops/sec", "millis");
        results.forEach(r -> System.out.printf("  %-16s %,12d %8d ms%n",
                r.strategy(), r.opsPerSecond(), r.elapsedMillis()));
        System.out.println();

        assertThat(results).allSatisfy(r ->
                assertThat(r.finalValue()).isEqualTo(EXPECTED));
    }
}
