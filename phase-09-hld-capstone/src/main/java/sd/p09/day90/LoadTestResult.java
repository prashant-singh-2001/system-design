package sd.p09.day90;

import java.time.Duration;

/** What one load-test run produced. */
public record LoadTestResult(int requests, int succeeded, int failed,
                             Duration p50, Duration p99, double cacheHitRatio) {

    public double availability() {
        return requests == 0 ? 1.0 : (double) succeeded / requests;
    }

    public String summary() {
        return String.format(
                "n=%,d  availability=%.3f%%  p50=%.3fms  p99=%.3fms  cacheHitRatio=%.1f%%",
                requests, availability() * 100,
                p50.toNanos() / 1_000_000.0, p99.toNanos() / 1_000_000.0,
                cacheHitRatio * 100);
    }
}
