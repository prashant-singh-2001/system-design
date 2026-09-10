package sd.p01.day01;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sd.p01.day01.LatencyLab.Measurement;

import static org.assertj.core.api.Assertions.assertThat;

class Day01LatencyTest {

    /** 64 MB - comfortably larger than any L3 cache, so misses are real. */
    private static final int SIZE = 1 << 24;

    /** 4096 ints = 16 KB apart. Every access is a different cache line and a different page. */
    private static final int STRIDE = 4096;

    @Test
    @DisplayName("both traversals visit every element, so the checksums must match")
    void bothTraversalsCoverTheWholeArray() {
        int[] data = LatencyLab.filledArray(1024);

        Measurement sequential = LatencyLab.sequentialSum(data);
        Measurement strided = LatencyLab.stridedSum(data, 64);

        long expected = 0;
        for (int value : data) {
            expected += value;
        }

        assertThat(sequential.checksum())
                .as("sequentialSum must sum every element")
                .isEqualTo(expected);
        assertThat(strided.checksum())
                .as("stridedSum must visit every element exactly once, just in a different order")
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("sequential access is measurably faster than strided access")
    void sequentialBeatsStrided() {
        int[] data = LatencyLab.filledArray(SIZE);

        Measurement sequential = LatencyLab.bestOf(3, () -> LatencyLab.sequentialSum(data));
        Measurement strided = LatencyLab.bestOf(3, () -> LatencyLab.stridedSum(data, STRIDE));

        System.out.printf("  sequential : %,d ns  (%.2f ns/element)%n",
                sequential.elapsedNanos(), sequential.nanosPerElement(SIZE));
        System.out.printf("  strided    : %,d ns  (%.2f ns/element)%n",
                strided.elapsedNanos(), strided.nanosPerElement(SIZE));
        System.out.printf("  ratio      : %.1fx%n",
                (double) strided.elapsedNanos() / sequential.elapsedNanos());

        assertThat(strided.elapsedNanos())
                .as("""
                        Strided access over a 64 MB array should be slower than sequential access.
                        If it is not, check that stridedSum really is jumping by the stride rather
                        than walking the array in order.""")
                .isGreaterThan(sequential.elapsedNanos());
    }
}
