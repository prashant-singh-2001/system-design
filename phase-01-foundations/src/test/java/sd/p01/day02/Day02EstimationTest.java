package sd.p01.day02;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day02EstimationTest {

    private final SystemProfile twitter = SystemProfile.twitterLike();

    @Test
    @DisplayName("200M DAU x 2 writes / 100k seconds = 4,000 writes per second")
    void writeQps() {
        assertThat(CapacityEstimator.writeQps(twitter)).isEqualTo(4_000);
    }

    @Test
    @DisplayName("200M DAU x 100 reads / 100k seconds = 200,000 reads per second")
    void readQps() {
        assertThat(CapacityEstimator.readQps(twitter)).isEqualTo(200_000);
    }

    @Test
    @DisplayName("peak is a multiple of average, rounded to whole requests")
    void peakQps() {
        assertThat(CapacityEstimator.peakQps(4_000, 2.5)).isEqualTo(10_000);
        assertThat(CapacityEstimator.peakQps(200_000, 3)).isEqualTo(600_000);
    }

    @Test
    @DisplayName("50:1 reads to writes - the number that says 'design the read path first'")
    void readWriteRatio() {
        assertThat(CapacityEstimator.readWriteRatio(twitter)).isEqualTo(50.0);
    }

    @Test
    @DisplayName("400M writes/day x 300 bytes = 120 GB of new data every day")
    void storagePerDay() {
        assertThat(CapacityEstimator.storageBytesPerDay(twitter)).isEqualTo(120_000_000_000L);
    }

    @Test
    @DisplayName("120 GB/day x 365 x 5 years x 3 replicas")
    void totalStorage() {
        long expected = 120_000_000_000L * 365 * 5 * 3;
        long actual = CapacityEstimator.totalStorageBytes(twitter);

        System.out.printf("  five-year replicated footprint: %s%n",
                CapacityEstimator.humanBytes(actual));

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("4,000 writes/s x 300 bytes = 1.2 MB/s inbound")
    void ingress() {
        assertThat(CapacityEstimator.ingressBytesPerSecond(twitter)).isEqualTo(1_200_000L);
    }

    @Test
    @DisplayName("a replication factor below 1 is not a thing")
    void rejectsNonsense() {
        assertThatThrownBy(() -> new SystemProfile(1, 1, 1, 1, 1, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
