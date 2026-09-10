package sd.p05.day45;

import java.time.Instant;

/** When one query started and finished - the raw material for measuring how much a pool overlaps. */
public record QueryTiming(Instant start, Instant end) {
}
