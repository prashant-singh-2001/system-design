package sd.p06.day60;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO(day60): percentiles, because an average latency tells you almost nothing.
 *
 * <p>Consider 99 requests at 1 ms and one at 1,000 ms. The mean is about 11 ms, which sounds
 * fine and describes nobody's experience: 99 people had a fast page and one waited a second.
 * Averages hide exactly the tail you are being paid to care about.
 *
 * <p>Worse, on a page that makes 100 backend calls, a p99 backend latency is hit by roughly
 * <b>63%</b> of page loads. The tail is not an edge case; at fan-out it is the common case. That
 * arithmetic - {@code 1 - 0.99^100} - is the single best argument for measuring percentiles, and
 * it is worth being able to produce it on demand.
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code record} - store one measurement</li>
 *   <li>{@code percentile(p)} - the nearest-rank method: sort, then take index
 *       {@code ceil(p/100 x n) - 1}, clamped into range. For p=50 over 100 samples that is
 *       index 49. Reject p outside (0, 100].</li>
 *   <li>{@code mean}</li>
 *   <li>{@code count}</li>
 * </ul>
 *
 * <p>An empty recorder returns {@link Duration#ZERO} rather than throwing - a metrics call
 * should never be the thing that breaks a request path.
 *
 * <p>Note this keeps every sample, which is fine for a benchmark and wrong for production. Real
 * systems use HDR histograms or t-digests: bounded memory, approximate quantiles. Knowing why
 * that trade exists is the takeaway.
 */
public final class LatencyRecorder {

    private final List<Duration> samples = new ArrayList<>();

    public void record(Duration latency) {
        throw new UnsupportedOperationException("TODO(day60): store the sample");
    }

    public Duration percentile(double p) {
        throw new UnsupportedOperationException("TODO(day60): nearest-rank percentile");
    }

    public Duration mean() {
        throw new UnsupportedOperationException("TODO(day60): arithmetic mean");
    }

    public int count() {
        return samples.size();
    }

    /** GIVEN - a one-line summary for benchmark output. */
    public String summary(String label) {
        return String.format("%-18s n=%,7d  mean=%6.2fms  p50=%6.2fms  p95=%6.2fms  p99=%6.2fms",
                label, count(), millis(mean()), millis(percentile(50)),
                millis(percentile(95)), millis(percentile(99)));
    }

    private static double millis(Duration duration) {
        return duration.toNanos() / 1_000_000.0;
    }
}
