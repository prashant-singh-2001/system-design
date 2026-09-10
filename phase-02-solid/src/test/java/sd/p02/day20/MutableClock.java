package sd.p02.day20;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/**
 * GIVEN - a clock you control. This little class is the entire reason to inject time:
 * with it, "what is the fee after three weeks" is a one-line test instead of a three-week wait.
 */
final class MutableClock extends Clock {

    private Instant now;
    private final ZoneId zone;

    MutableClock(Instant start, ZoneId zone) {
        this.now = start;
        this.zone = zone;
    }

    void advance(Duration duration) {
        now = now.plus(duration);
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId other) {
        return new MutableClock(now, other);
    }

    @Override
    public Instant instant() {
        return now;
    }
}
