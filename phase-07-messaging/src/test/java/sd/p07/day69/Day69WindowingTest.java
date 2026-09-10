package sd.p07.day69;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class Day69WindowingTest {

    private static final Duration MINUTE = Duration.ofMinutes(1);

    private static TimestampedEvent at(String key, long value, String isoTime) {
        return new TimestampedEvent(key, value, Instant.parse(isoTime));
    }

    private static final List<TimestampedEvent> EVENTS = List.of(
            at("gb", 10, "2026-03-01T12:00:05Z"),
            at("gb", 20, "2026-03-01T12:00:40Z"),
            at("us", 30, "2026-03-01T12:00:50Z"),
            at("gb", 40, "2026-03-01T12:01:10Z"),
            at("us", 50, "2026-03-01T12:01:30Z"),
            at("gb", 60, "2026-03-01T12:02:00Z"));

    @Test
    @DisplayName("an instant floors to its window boundary")
    void windowBoundaries() {
        assertThat(WindowedAggregator.windowStart(Instant.parse("2026-03-01T12:00:37Z"), MINUTE))
                .isEqualTo(Instant.parse("2026-03-01T12:00:00Z"));
        assertThat(WindowedAggregator.windowStart(Instant.parse("2026-03-01T12:00:00Z"), MINUTE))
                .as("a boundary belongs to its own window - half-open intervals")
                .isEqualTo(Instant.parse("2026-03-01T12:00:00Z"));
        assertThat(WindowedAggregator.windowStart(
                Instant.parse("2026-03-01T12:07:59Z"), Duration.ofMinutes(5)))
                .isEqualTo(Instant.parse("2026-03-01T12:05:00Z"));
    }

    @Test
    @DisplayName("tumbling: every event lands in exactly one window")
    void tumblingWindows() {
        Map<Window, Map<String, Long>> result = WindowedAggregator.tumblingSum(EVENTS, MINUTE);

        result.forEach((window, sums) -> System.out.printf("  %s -> %s%n", window, sums));

        assertThat(result).hasSize(3);

        Window first = result.keySet().iterator().next();
        assertThat(first.start()).isEqualTo(Instant.parse("2026-03-01T12:00:00Z"));
        assertThat(result.get(first)).containsEntry("gb", 30L).containsEntry("us", 30L);
    }

    @Test
    @DisplayName("tumbling windows are emitted in ascending time order")
    void tumblingIsOrdered() {
        List<Window> windows = List.copyOf(
                WindowedAggregator.tumblingSum(EVENTS, MINUTE).keySet());

        assertThat(windows).hasSize(3);
        assertThat(windows.get(0).start()).isBefore(windows.get(1).start());
        assertThat(windows.get(1).start()).isBefore(windows.get(2).start());
    }

    @Test
    @DisplayName("the totals are conserved - windowing partitions, it does not lose")
    void nothingIsLost() {
        long windowedTotal = WindowedAggregator.tumblingSum(EVENTS, MINUTE).values().stream()
                .flatMap(m -> m.values().stream())
                .mapToLong(Long::longValue)
                .sum();

        assertThat(windowedTotal)
                .as("an aggregation that changes the total is a bug, not an optimisation")
                .isEqualTo(EVENTS.stream().mapToLong(TimestampedEvent::value).sum());
    }

    @Test
    @DisplayName("hopping: overlapping windows mean an event is counted several times")
    void hoppingWindows() {
        Map<Window, Map<String, Long>> result = WindowedAggregator.hoppingSum(
                EVENTS, Duration.ofMinutes(2), MINUTE);

        result.forEach((window, sums) -> System.out.printf("  %s -> %s%n", window, sums));

        assertThat(result.size())
                .as("2-minute windows advancing every minute produce more windows than tumbling")
                .isGreaterThan(3);

        long hoppingTotal = result.values().stream()
                .flatMap(m -> m.values().stream())
                .mapToLong(Long::longValue)
                .sum();

        assertThat(hoppingTotal)
                .as("""
                        Each event belongs to several overlapping windows, so the sum of window
                        totals legitimately exceeds the stream total. Smoother output, more
                        computation, more state - that is the trade.""")
                .isGreaterThan(EVENTS.stream().mapToLong(TimestampedEvent::value).sum());
    }

    @Test
    @DisplayName("event time, not processing time: a delayed event still lands in its own window")
    void eventTimeIsWhatMatters() {
        // This arrived minutes late, but it happened at 12:00:30.
        TimestampedEvent delayed = at("gb", 99, "2026-03-01T12:00:30Z");

        Map<Window, Map<String, Long>> result =
                WindowedAggregator.tumblingSum(List.of(delayed), MINUTE);

        assertThat(result.keySet().iterator().next().start())
                .as("""
                        Windowing by processing time would have put this in whatever minute we
                        happened to see it - silently wrong whenever anything is delayed, which
                        is always.""")
                .isEqualTo(Instant.parse("2026-03-01T12:00:00Z"));
    }

    @Test
    @DisplayName("the late-data policy is one comparison")
    void lateness() {
        Instant watermark = Instant.parse("2026-03-01T12:10:00Z");
        Duration allowed = Duration.ofMinutes(5);

        assertThat(WindowedAggregator.isLate(
                Instant.parse("2026-03-01T12:07:00Z"), watermark, allowed))
                .as("three minutes behind the watermark, inside the five-minute grace")
                .isFalse();
        assertThat(WindowedAggregator.isLate(
                Instant.parse("2026-03-01T12:02:00Z"), watermark, allowed))
                .as("eight minutes behind - beyond the grace period, so it is dropped")
                .isTrue();
        assertThat(WindowedAggregator.isLate(
                Instant.parse("2026-03-01T12:05:00Z"), watermark, allowed))
                .as("exactly at the boundary is still accepted")
                .isFalse();
    }

    @Test
    @DisplayName("an empty stream produces no windows")
    void emptyStream() {
        assertThat(WindowedAggregator.tumblingSum(List.of(), MINUTE)).isEmpty();
    }
}
