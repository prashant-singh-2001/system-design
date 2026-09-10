package sd.p04.day33;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * GIVEN - a {@link Clock} a test can move forward by hand, so burst and refill behaviour is
 * checked by advancing simulated time, never by actually sleeping.
 */
final class TestClock extends Clock {

    private Instant now = Instant.parse("2024-01-01T00:00:00Z");

    void advance(Duration duration) {
        now = now.plus(duration);
    }

    @Override
    public Instant instant() {
        return now;
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        throw new UnsupportedOperationException("not needed for these tests");
    }
}
