package sd.p07.day66;

import java.time.Duration;

/** What happened when a fast producer met a slow consumer. */
public record BackpressureResult(int offered, int accepted, int dropped, int processed,
                                 Duration elapsed, int maxQueueDepth) {

    public double dropRate() {
        return offered == 0 ? 0 : (double) dropped / offered;
    }
}
