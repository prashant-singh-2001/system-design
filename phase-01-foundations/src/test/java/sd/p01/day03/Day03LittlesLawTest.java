package sd.p01.day03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class Day03LittlesLawTest {

    @Test
    @DisplayName("1000 req/s at 50 ms latency needs 50 concurrent workers")
    void concurrency() {
        assertThat(LittlesLaw.concurrencyNeeded(1_000, 0.05)).isEqualTo(50.0, within(1e-9));
    }

    @Test
    @DisplayName("50 workers at 50 ms latency can never exceed 1000 req/s")
    void throughputCeiling() {
        assertThat(LittlesLaw.maxThroughput(50, 0.05)).isEqualTo(1_000.0, within(1e-9));
        // Halving latency doubles the ceiling with the same pool - usually the cheaper fix.
        assertThat(LittlesLaw.maxThroughput(50, 0.025)).isEqualTo(2_000.0, within(1e-9));
    }

    @Test
    void utilisation() {
        assertThat(LittlesLaw.utilisation(80, 100)).isEqualTo(0.8, within(1e-9));
    }

    @Test
    @DisplayName("the knee: 99% utilisation costs 20x the latency of 80%")
    void theKnee() {
        double at80 = LittlesLaw.averageResponseTime(80, 100);
        double at99 = LittlesLaw.averageResponseTime(99, 100);

        assertThat(at80).isEqualTo(0.05, within(1e-9));
        assertThat(at99).isEqualTo(1.0, within(1e-9));

        assertThat(at99 / at80)
                .as("this ratio is the entire argument for headroom")
                .isEqualTo(20.0, within(1e-6));

        LittlesLaw.printUtilisationCurve(100);
    }

    @Test
    @DisplayName("queue length also explodes at the knee")
    void queueLength() {
        // rho = 0.5 -> 0.25/0.5 = 0.5 waiting
        assertThat(LittlesLaw.averageQueueLength(50, 100)).isEqualTo(0.5, within(1e-9));
        // rho = 0.9 -> 0.81/0.1 = 8.1 waiting
        assertThat(LittlesLaw.averageQueueLength(90, 100)).isEqualTo(8.1, within(1e-9));
    }

    @Test
    @DisplayName("an overloaded queue has no finite answer, so say so")
    void unstableQueueIsRejected() {
        assertThatThrownBy(() -> LittlesLaw.averageResponseTime(100, 100))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> LittlesLaw.averageQueueLength(120, 100))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
