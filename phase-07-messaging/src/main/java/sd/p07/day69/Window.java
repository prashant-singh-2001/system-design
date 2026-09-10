package sd.p07.day69;

import java.time.Instant;

/** One time bucket, half-open: {@code [start, end)}. */
public record Window(Instant start, Instant end) {

    public boolean contains(Instant time) {
        return !time.isBefore(start) && time.isBefore(end);
    }

    @Override
    public String toString() {
        return start + " .. " + end;
    }
}
