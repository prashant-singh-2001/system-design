package sd.p09.day86;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class Day86StreamingTest {

    @Test
    @DisplayName("one upload becomes six files - the ladder is what you actually store")
    void ladderStorage() {
        long oneMinute = StreamingMath.storageBytesForLadder(
                Rendition.LADDER, Duration.ofMinutes(1));
        long justOneRendition = StreamingMath.storageBytesForLadder(
                List.of(new Rendition("1080p", 1080, 5_000)), Duration.ofMinutes(1));

        System.out.printf("  1 minute of video: %,d bytes across the ladder (%,d for 1080p alone)%n",
                oneMinute, justOneRendition);

        assertThat(oneMinute)
                .as("storing every rendition costs several times a single one")
                .isGreaterThan(justOneRendition * 4);
    }

    @Test
    @DisplayName("a minute of 1080p is about 37 MB")
    void singleRenditionSize() {
        long bytes = StreamingMath.storageBytesForLadder(
                List.of(new Rendition("1080p", 1080, 5_000)), Duration.ofMinutes(1));

        assertThat(bytes).isCloseTo(37_500_000L, Percentage.withPercentage(1));
    }

    @Test
    @DisplayName("egress dwarfs storage - which is why every video design is a caching design")
    void egressDominates() {
        Rendition hd = new Rendition("1080p", 1080, 5_000);

        long storedOnce = StreamingMath.storageBytesForLadder(
                List.of(hd), Duration.ofMinutes(10));
        long servedMonthly = StreamingMath.egressBytesPerMonth(
                hd, 1_000_000, Duration.ofMinutes(6));

        System.out.printf("  stored once : %,d bytes%n", storedOnce);
        System.out.printf("  egress/month: %,d bytes for 1M views%n", servedMonthly);

        assertThat(servedMonthly)
                .as("""
                        One file, stored once, served a million times. Egress is usually the
                        dominant cost of a video platform - larger than storage, larger than
                        compute - and that single fact is why CDNs exist.""")
                .isGreaterThan(storedOnce * 100_000);
    }

    @Test
    @DisplayName("the player picks the best rendition it can actually sustain")
    void adaptiveSelection() {
        assertThat(StreamingMath.selectRendition(Rendition.LADDER, 10_000, 0.8).label())
                .as("8,000 kbps of usable throughput reaches 1080p but not 4K")
                .isEqualTo("1080p");
        assertThat(StreamingMath.selectRendition(Rendition.LADDER, 1_200, 0.8).label())
                .isEqualTo("480p");
        assertThat(StreamingMath.selectRendition(Rendition.LADDER, 100_000, 0.8).label())
                .isEqualTo("4K");
    }

    @Test
    @DisplayName("the safety factor is why players do not stall on every fluctuation")
    void safetyFactorMatters() {
        int measured = 5_000;

        assertThat(StreamingMath.selectRendition(Rendition.LADDER, measured, 1.0).label())
                .as("selecting exactly your measured bandwidth")
                .isEqualTo("1080p");
        assertThat(StreamingMath.selectRendition(Rendition.LADDER, measured, 0.8).label())
                .as("""
                        Measured throughput is a noisy average of a fluctuating value. A rendition
                        that needs all of it guarantees stalls, so players deliberately
                        under-select.""")
                .isEqualTo("720p");
    }

    @Test
    @DisplayName("a terrible connection gets the lowest rendition, never a stall")
    void neverStall() {
        assertThat(StreamingMath.selectRendition(Rendition.LADDER, 50, 0.8).label())
                .as("a soft picture beats a spinner, always")
                .isEqualTo("240p");
    }

    @Test
    @DisplayName("chunking is what makes mid-stream switching possible")
    void chunking() {
        assertThat(StreamingMath.chunkCount(Duration.ofMinutes(10), Duration.ofSeconds(4)))
                .isEqualTo(150);
        assertThat(StreamingMath.chunkCount(Duration.ofSeconds(10), Duration.ofSeconds(4)))
                .as("a partial final chunk still counts")
                .isEqualTo(3);
        assertThat(StreamingMath.chunkCount(Duration.ZERO, Duration.ofSeconds(4))).isZero();
    }
}
