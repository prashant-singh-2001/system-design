package sd.p01.day01;

import java.util.function.Supplier;

/**
 * Day 1 - the memory hierarchy, measured rather than memorised.
 *
 * <p>Both methods below must visit every element of the array exactly once, so they
 * produce an identical checksum. The only difference is the ORDER of the visits.
 * That difference alone is worth 5-20x in wall-clock time, and that fact underpins
 * LSM trees, batch writes, and most of why caching works at all.
 */
public final class LatencyLab {

    private LatencyLab() {
    }

    /** The result of one measured pass: how long it took, and proof it did the work. */
    public record Measurement(long elapsedNanos, long checksum) {

        public double nanosPerElement(int elements) {
            return (double) elapsedNanos / elements;
        }
    }

    /**
     * TODO(day01): visit data[0], data[1], data[2] ... in order, summing into a long.
     * Time it with System.nanoTime() and return a Measurement.
     *
     * <p>This is the cache-FRIENDLY traversal: the CPU prefetcher sees the pattern and
     * loads the next cache line before you ask for it. One 64-byte line holds 16 ints,
     * so you pay for roughly one memory fetch per 16 elements.
     */
    public static Measurement sequentialSum(int[] data) {
        long start = System.nanoTime();
        long checksum = 0;
        for (int i = 0; i < data.length; i++) {
            checksum += data[i];
        }
        long elapsed = System.nanoTime() - start;
        return new Measurement(elapsed, checksum);
        // throw new UnsupportedOperationException("TODO(day01): implement sequentialSum");
    }

    /**
     * TODO(day01): visit every element exactly once, but jumping by {@code stride}.
     *
     * <p>The standard shape is an outer loop over offsets 0..stride-1 and an inner loop
     * stepping i += stride. Every element is touched once, so the checksum must match
     * sequentialSum, but each access lands on a different cache line and the prefetcher
     * cannot help you.
     *
     * <p>This is the cache-HOSTILE traversal. It is what a random-access pattern over a
     * large dataset costs you, and it is why "just add an index" is not always the answer.
     */
    public static Measurement stridedSum(int[] data, int stride) {
        long start = System.nanoTime();
        long checksum = 0;
        for (int offset = 0; offset < stride; offset++) {
            for (int i = offset; i < data.length; i += stride) {
                checksum += data[i];
            }
        }
        long elapsed = System.nanoTime() - start;
        return new Measurement(elapsed, checksum);
        // throw new UnsupportedOperationException("TODO(day01): implement stridedSum");
    }

    // ---------------------------------------------------------------- given helpers

    /**
     * Runs a measurement several times and keeps the fastest. The JIT needs a few passes
     * to compile the hot loop; the first run measures the interpreter, not your code.
     */
    public static Measurement bestOf(int runs, Supplier<Measurement> measurement) {
        Measurement best = null;
        for (int i = 0; i < runs; i++) {
            Measurement m = measurement.get();
            if (best == null || m.elapsedNanos() < best.elapsedNanos()) {
                best = m;
            }
        }
        return best;
    }

    /** An array of the given size, filled with values whose sum is easy to predict. */
    public static int[] filledArray(int size) {
        int[] data = new int[size];
        for (int i = 0; i < size; i++) {
            data[i] = i & 0xFF;
        }
        return data;
    }
}
