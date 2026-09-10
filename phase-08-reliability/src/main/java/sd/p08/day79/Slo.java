package sd.p08.day79;

import java.time.Duration;

/**
 * A service level objective: the target, and the window it is measured over.
 *
 * @param target availability as a fraction, e.g. {@code 0.999} for "three nines"
 */
public record Slo(String name, double target, Duration window) {

    public Slo {
        if (target <= 0 || target >= 1) {
            throw new IllegalArgumentException("target must be in (0, 1): " + target);
        }
        if (window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("window must be positive");
        }
    }

    /**
     * TODO(day79): how much downtime the target permits over the window.
     *
     * <p>The numbers are worth knowing by heart, because they reframe the argument. Over 30 days:
     * <ul>
     *   <li>99%     -> about 7 hours 12 minutes</li>
     *   <li>99.9%   -> about 43 minutes</li>
     *   <li>99.99%  -> about 4 minutes 19 seconds</li>
     *   <li>99.999% -> about 26 seconds</li>
     * </ul>
     *
     * <p>Each extra nine costs roughly ten times as much engineering and permits a tenth of the
     * downtime. "Five nines" is 26 seconds a month - less than a single deploy, or one bad DNS
     * change. Putting the number next to the request makes the conversation honest.
     */
    public Duration allowedDowntime() {
        throw new UnsupportedOperationException("TODO(day79): window x (1 - target)");
    }

    public static Slo thirtyDay(String name, double target) {
        return new Slo(name, target, Duration.ofDays(30));
    }
}
