package sd.p07.day69;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO(day69): stream processing - aggregating an unbounded stream.
 *
 * <p>A batch job has an end; a stream does not. "The average over all of it" is not a question
 * you can answer about something infinite, so you answer over <b>windows</b> instead. Windowing
 * is what makes an unbounded stream tractable, and every stream processor is built around it.
 *
 * <p><b>Tumbling windows</b> are fixed and non-overlapping - every event belongs to exactly one.
 * "Orders per minute" is a tumbling window.
 *
 * <p><b>Hopping (sliding) windows</b> overlap: a 5-minute window advancing every minute means
 * each event lands in five of them. "The 5-minute moving average, updated every minute" is a
 * hopping window. Smoother output, more computation and more state.
 *
 * <p>Then the idea that makes stream processing genuinely hard: <b>event time versus processing
 * time.</b> An event's {@code eventTime} is when it happened; processing time is when you saw it.
 * They differ because phones go through tunnels, networks retry, and consumers lag. Window by
 * processing time and your minute buckets are wrong whenever anything is delayed - and they are
 * silently wrong, which is worse.
 *
 * <p>Window by EVENT time and the buckets are correct, but you face a new question with no clean
 * answer: when is a window finished? An event for 12:00 may arrive at 12:07. Wait forever and you
 * never emit; close immediately and you drop late data. Real systems use <b>watermarks</b> - a
 * heuristic "we believe we have seen everything up to T" - plus an explicit policy for
 * stragglers, which is {@code allowedLateness} below.
 *
 * <p>There is no correct answer here, only a stated trade between latency and completeness. Being
 * able to say that plainly is the mark of someone who has run a streaming pipeline.
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code windowStart} - floor an instant to a window boundary, measured from the epoch:
 *       {@code epochMilli - floorMod(epochMilli, sizeMillis)}. Using {@code floorMod} keeps it
 *       correct for instants before the epoch.</li>
 *   <li>{@code tumblingSum} - sum values per (window, key), keeping windows in ascending order.
 *       Use a {@code LinkedHashMap} so the output order is deterministic and testable.</li>
 *   <li>{@code hoppingSum} - the same, but each event belongs to every window that contains it,
 *       given {@code size} and {@code advance}.</li>
 *   <li>{@code isLate} - true when {@code eventTime} is before
 *       {@code watermark - allowedLateness}. That single comparison is the whole late-data policy.</li>
 * </ul>
 */
public final class WindowedAggregator {

    private WindowedAggregator() {
    }

    public static Instant windowStart(Instant eventTime, Duration size) {
        throw new UnsupportedOperationException("TODO(day69): floor to the window boundary");
    }

    public static Map<Window, Map<String, Long>> tumblingSum(List<TimestampedEvent> events,
                                                             Duration size) {
        throw new UnsupportedOperationException("TODO(day69): one window per event");
    }

    public static Map<Window, Map<String, Long>> hoppingSum(List<TimestampedEvent> events,
                                                            Duration size, Duration advance) {
        throw new UnsupportedOperationException("TODO(day69): every window that contains the event");
    }

    public static boolean isLate(Instant eventTime, Instant watermark, Duration allowedLateness) {
        throw new UnsupportedOperationException("TODO(day69): the whole late-data policy, in one line");
    }
}
