package sd.p09.day86;

import java.time.Duration;
import java.util.List;

/**
 * TODO(day86): the two numbers that shape a video system, and the one algorithm the player runs.
 *
 * <p><b>Storage.</b> Video is not stored once - it is stored once per rendition. A single upload
 * becomes six files, and the ladder's total is what you actually pay for. This is why transcoding
 * is asynchronous and why nobody transcodes on the upload path: it is minutes of CPU per minute of
 * video, and it must not block the user.
 *
 * <p><b>Bandwidth.</b> Egress is usually the dominant cost of a video platform - larger than
 * storage, larger than compute. That single fact is why CDNs exist, and why every serious video
 * design is really a caching design.
 *
 * <p><b>Adaptive bitrate.</b> The player measures its throughput and picks the highest rendition it
 * can sustain, switching mid-stream as the network changes. What makes that possible is
 * <b>chunking</b>: the video is cut into a few seconds each, so the player can choose a different
 * rendition for the very next chunk. Without chunking a network dip means a stall; with it, the
 * picture softens and playback continues.
 *
 * <p>Note the safety factor. Selecting a rendition needing exactly your measured bandwidth
 * guarantees stalls, because the measurement is a noisy average of a fluctuating value. Players
 * deliberately under-select.
 *
 * <p>Implement:
 * <ul>
 *   <li>{@code storageBytesForLadder} - sum over the ladder of
 *       {@code bitrateKbps x 1000 / 8 x seconds}.</li>
 *   <li>{@code egressBytesPerMonth} - {@code views x watchSeconds x bytes per second}.</li>
 *   <li>{@code selectRendition} - the highest rendition at or below
 *       {@code measuredKbps x safetyFactor}; if none fits, the lowest. A soft picture beats a
 *       stall, always.</li>
 *   <li>{@code chunkCount} - how many chunks a video becomes, rounding up.</li>
 * </ul>
 */
public final class StreamingMath {

    private StreamingMath() {
    }

    public static long storageBytesForLadder(List<Rendition> ladder, Duration length) {
        throw new UnsupportedOperationException("TODO(day86): sum the ladder");
    }

    public static long egressBytesPerMonth(Rendition rendition, long viewsPerMonth,
                                           Duration averageWatch) {
        throw new UnsupportedOperationException("TODO(day86): views x seconds x bytes per second");
    }

    public static Rendition selectRendition(List<Rendition> ladder, int measuredKbps,
                                            double safetyFactor) {
        throw new UnsupportedOperationException("TODO(day86): highest that fits, else the lowest");
    }

    public static long chunkCount(Duration length, Duration chunkLength) {
        throw new UnsupportedOperationException("TODO(day86): round up");
    }
}
