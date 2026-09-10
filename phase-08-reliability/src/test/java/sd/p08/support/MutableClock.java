package sd.p08.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** GIVEN - a clock you control, so timing behaviour is testable without sleeping. */
public final class MutableClock extends Clock {

    private Instant now;
    private final ZoneId zone;

    public MutableClock(Instant start, ZoneId zone) {
        this.now = start;
        this.zone = zone;
    }

    public static MutableClock startingAt(String isoInstant) {
        return new MutableClock(Instant.parse(isoInstant), ZoneOffset.UTC);
    }

    public void advance(Duration duration) {
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
