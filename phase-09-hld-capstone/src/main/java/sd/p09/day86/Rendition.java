package sd.p09.day86;

import java.util.List;

/**
 * One encoding of a video: a resolution, and the bandwidth it needs.
 *
 * @param bitrateKbps sustained bandwidth required to play this rendition without stalling
 */
public record Rendition(String label, int heightPixels, int bitrateKbps) {

    /** A conventional adaptive-bitrate ladder. */
    public static final List<Rendition> LADDER = List.of(
            new Rendition("240p", 240, 400),
            new Rendition("360p", 360, 800),
            new Rendition("480p", 480, 1_400),
            new Rendition("720p", 720, 2_800),
            new Rendition("1080p", 1080, 5_000),
            new Rendition("4K", 2160, 16_000));
}
