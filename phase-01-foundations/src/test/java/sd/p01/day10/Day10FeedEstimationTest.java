package sd.p01.day10;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Day10FeedEstimationTest {

    @Test
    @DisplayName("4,000 posts/s x 200 followers = 800,000 feed writes/s")
    void fanoutIsTheNumberThatDecidesTheDesign() {
        long fanout = FeedEstimator.fanoutWritesPerSecond(4_000, 200);

        System.out.printf("%n  fanout-on-write load: %,d writes/second%n", fanout);
        System.out.printf("  ... from only %,d posts/second%n%n", 4_000);

        assertThat(fanout).isEqualTo(800_000L);
    }

    @Test
    @DisplayName("size the cache for the hot 20%, not the whole dataset")
    void hotDataset() {
        // 2.8 billion cached feed items x 300 bytes, of which 20% is hot
        long hot = FeedEstimator.hotDatasetBytes(2_800_000_000L, 300, 0.2);

        assertThat(hot).isEqualTo(168_000_000_000L);   // 168 GB
    }

    @Test
    @DisplayName("capacity always rounds up - 2.6 servers is 3 servers")
    void roundingUp() {
        assertThat(FeedEstimator.serversNeeded(168_000_000_000L, 64_000_000_000L)).isEqualTo(3);
        assertThat(FeedEstimator.serversNeeded(128_000_000_000L, 64_000_000_000L)).isEqualTo(2);
        assertThat(FeedEstimator.serversNeeded(1, 64_000_000_000L)).isEqualTo(1);
        assertThat(FeedEstimator.serversNeeded(0, 64_000_000_000L)).isEqualTo(0);
    }

    @Test
    @DisplayName("500,000 peak QPS at 5,000 QPS per server = 100 servers")
    void appServers() {
        assertThat(FeedEstimator.appServersNeeded(500_000, 5_000)).isEqualTo(100);
        assertThat(FeedEstimator.appServersNeeded(500_001, 5_000)).isEqualTo(101);
    }

    @Test
    @DisplayName("celebrities get fanout-on-read; everyone else gets fanout-on-write")
    void theHybridRule() {
        long threshold = 100_000;

        assertThat(FeedEstimator.useFanoutOnRead(150, threshold))
                .as("an ordinary account is cheap to fan out on write")
                .isFalse();
        assertThat(FeedEstimator.useFanoutOnRead(50_000_000L, threshold))
                .as("50 million inbox writes for one post is a write storm")
                .isTrue();
    }
}
