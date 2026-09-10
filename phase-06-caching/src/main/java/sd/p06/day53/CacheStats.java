package sd.p06.day53;

/**
 * What a cache must tell you about itself.
 *
 * <p>Hit ratio is the number that decides whether a cache is worth its complexity. Below about
 * 80% you are paying the cost of two systems - and the consistency risk - for a modest win. The
 * only way to know is to measure, which is why every real cache exposes these counters and why
 * an uninstrumented cache is essentially unmanageable.
 */
public record CacheStats(long hits, long misses, long evictions, long expirations) {

    public long lookups() {
        return hits + misses;
    }

    public double hitRatio() {
        return lookups() == 0 ? 0.0 : (double) hits / lookups();
    }

    @Override
    public String toString() {
        return String.format("hits=%d misses=%d evictions=%d expirations=%d hitRatio=%.1f%%",
                hits, misses, evictions, expirations, hitRatio() * 100);
    }
}
