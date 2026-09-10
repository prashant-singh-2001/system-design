package sd.p04.day40;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** GIVEN - advance simulated time by hand; no real sleeping anywhere in this test. */
final class TestClock extends Clock {

    private Instant now = Instant.parse("2024-01-01T00:00:00Z");

    void advance(Duration duration) {
        now = now.plus(duration);
    }

    Instant now() {
        return now;
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
