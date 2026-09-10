package sd.p01.day02;

/**
 * Day 2 - turning a product description into numbers that drive design decisions.
 *
 * <p>The point is never precision. It is being right about the ORDER OF MAGNITUDE quickly
 * enough to make a decision, and knowing which single number matters most. Usually that
 * number is the read:write ratio.
 */
public final class CapacityEstimator {

    /**
     * A day is 86,400 seconds. We use 100,000. The 15% error is irrelevant next to the
     * uncertainty in every other input, and dividing by a power of ten in your head is
     * the whole trick.
     */
    public static final long SECONDS_PER_DAY = 100_000L;

    public static final int DAYS_PER_YEAR = 365;

    private CapacityEstimator() {
    }

    /** TODO(day02): average writes per second across the whole day. */
    public static long writeQps(SystemProfile p) {
        return (long) (p.dailyActiveUsers() * p.writesPerUserPerDay() / SECONDS_PER_DAY);
    }

    /** TODO(day02): average reads per second across the whole day. */
    public static long readQps(SystemProfile p) {
        return (long) (p.dailyActiveUsers() * p.readsPerUserPerDay() / SECONDS_PER_DAY);
    }

    /**
     * TODO(day02): traffic is not flat - it peaks during waking hours.
     * Multiply and round to a whole number of requests.
     */
    public static long peakQps(long averageQps, double peakMultiplier) {
        return Math.round(averageQps * peakMultiplier);
    }

    /**
     * TODO(day02): reads divided by writes.
     *
     * <p>This is the most important number you will produce. Above roughly 10:1 you are
     * designing a read path and should reach for caching, replicas and denormalisation.
     * Below 1:1 you are designing a write path and should think about batching, LSM
     * storage and partitioning.
     */
    public static double readWriteRatio(SystemProfile p) {
        return (double) readQps(p) / writeQps(p);
    }

    /** TODO(day02): new bytes written per day, before replication. */
    public static long storageBytesPerDay(SystemProfile p) {
        return (long) (p.dailyActiveUsers() * p.writesPerUserPerDay() * p.bytesPerWrite());
    }

    /** TODO(day02): total stored bytes over the retention window, including replication. */
    public static long totalStorageBytes(SystemProfile p) {
        return (long) (storageBytesPerDay(p) * p.retentionYears() * DAYS_PER_YEAR * p.replicationFactor());
    }

    /** TODO(day02): inbound bytes per second at average write load. */
    public static long ingressBytesPerSecond(SystemProfile p) {
        return (long) (writeQps(p) * p.bytesPerWrite());
    }

    // ---------------------------------------------------------------- given helpers

    /** Human-readable bytes, because "657000000000000" tells you nothing in an interview. */
    public static String humanBytes(long bytes) {
        String[] units = {"B", "KB", "MB", "GB", "TB", "PB"};
        double value = bytes;
        int unit = 0;
        while (value >= 1024 && unit < units.length - 1) {
            value /= 1024;
            unit++;
        }
        return String.format("%.1f %s", value, units[unit]);
    }
}
