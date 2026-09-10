package sd.p07.day66;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class Day66BackpressureTest {

    private static final Duration SLOW = Duration.ofMillis(2);

    @Test
    @DisplayName("the before-picture: an unbounded queue accepts everything and warns you of nothing")
    void unboundedQueueGrowsSilently() {
        int depth = BoundedPipeline.unboundedGrowth(100_000);

        System.out.printf("  unbounded queue reached %,d items with no consumer%n", depth);

        assertThat(depth)
                .as("""
                        Nothing failed. Nothing warned. In production this is memory climbing for
                        twenty minutes, then GC thrashing, then an OutOfMemoryError that takes down
                        the healthy work too. A throughput problem became an availability incident.""")
                .isEqualTo(100_000);
    }

    @Test
    @DisplayName("BLOCK: nothing is lost, and the producer is slowed to the consumer's rate")
    void blockingPropagatesBackpressure() throws InterruptedException {
        BackpressureResult result = BoundedPipeline.run(200, 20, SLOW, OverflowPolicy.BLOCK);

        System.out.printf("  BLOCK       : accepted %d, dropped %d, processed %d in %d ms%n",
                result.accepted(), result.dropped(), result.processed(), result.elapsed().toMillis());

        assertThat(result.dropped()).as("backpressure means waiting, not losing").isZero();
        assertThat(result.processed()).isEqualTo(200);
        assertThat(result.maxQueueDepth())
                .as("the bound is a promise about the worst case")
                .isLessThanOrEqualTo(20);
        assertThat(result.elapsed())
                .as("200 items at 2 ms each cannot finish faster than the consumer can work")
                .isGreaterThanOrEqualTo(Duration.ofMillis(300));
    }

    @Test
    @DisplayName("DROP_NEWEST: latency stays bounded because load is shed deliberately")
    void dropNewestShedsLoad() throws InterruptedException {
        BackpressureResult result =
                BoundedPipeline.run(2_000, 10, SLOW, OverflowPolicy.DROP_NEWEST);

        System.out.printf("  DROP_NEWEST : accepted %d, dropped %d (%.0f%%), processed %d%n",
                result.accepted(), result.dropped(), result.dropRate() * 100, result.processed());

        assertThat(result.dropped())
                .as("a fast producer against a 2 ms consumer must lose something")
                .isGreaterThan(0);
        assertThat(result.accepted() + result.dropped()).isEqualTo(2_000);
        assertThat(result.maxQueueDepth()).isLessThanOrEqualTo(10);
    }

    @Test
    @DisplayName("DROP_OLDEST: for data where fresh beats complete")
    void dropOldestKeepsRecentData() throws InterruptedException {
        BackpressureResult result =
                BoundedPipeline.run(2_000, 10, SLOW, OverflowPolicy.DROP_OLDEST);

        System.out.printf("  DROP_OLDEST : accepted %d, dropped %d, processed %d%n",
                result.accepted(), result.dropped(), result.processed());

        assertThat(result.dropped()).isGreaterThan(0);
        assertThat(result.maxQueueDepth())
                .as("the queue still never exceeds its bound")
                .isLessThanOrEqualTo(10);
    }

    @Test
    @DisplayName("a queue absorbs bursts; it cannot fix a sustained rate mismatch")
    void aQueueCannotFixARateMismatch() throws InterruptedException {
        BackpressureResult small = BoundedPipeline.run(1_000, 10, SLOW, OverflowPolicy.DROP_NEWEST);
        BackpressureResult large = BoundedPipeline.run(1_000, 500, SLOW, OverflowPolicy.DROP_NEWEST);

        System.out.printf("  capacity 10  -> dropped %d%n", small.dropped());
        System.out.printf("  capacity 500 -> dropped %d%n", large.dropped());

        assertThat(large.dropped())
                .as("""
                        A bigger buffer delays the problem, it does not solve it. If production
                        exceeds consumption on average, no buffer size saves you - it only changes
                        how long you wait before finding out.""")
                .isGreaterThan(0);
    }

    @Test
    @DisplayName("when the consumer keeps up, nothing is dropped under any policy")
    void noPressureNoDrops() throws InterruptedException {
        for (OverflowPolicy policy : OverflowPolicy.values()) {
            BackpressureResult result =
                    BoundedPipeline.run(50, 100, Duration.ofMillis(1), policy);

            assertThat(result.dropped())
                    .as("%s should not drop when there is room", policy)
                    .isZero();
            assertThat(result.processed()).isEqualTo(50);
        }
    }
}
