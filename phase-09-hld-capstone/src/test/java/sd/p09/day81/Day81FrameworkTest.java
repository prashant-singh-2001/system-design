package sd.p09.day81;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class Day81FrameworkTest {

    @Test
    @DisplayName("the phases add up to exactly 45 minutes")
    void budgetIsComplete() {
        assertThat(DesignClock.totalBudget())
                .as("a plan whose parts do not sum to the whole is a wish")
                .isEqualTo(Duration.ofMinutes(45));
    }

    @Test
    @DisplayName("each phase starts where the previous one ended")
    void phaseBoundaries() {
        assertThat(DesignClock.startOf(DesignPhase.SCOPE)).isEqualTo(Duration.ZERO);
        assertThat(DesignClock.startOf(DesignPhase.ESTIMATE)).isEqualTo(Duration.ofMinutes(5));
        assertThat(DesignClock.startOf(DesignPhase.API_AND_DATA)).isEqualTo(Duration.ofMinutes(10));
        assertThat(DesignClock.startOf(DesignPhase.ARCHITECTURE)).isEqualTo(Duration.ofMinutes(15));
        assertThat(DesignClock.startOf(DesignPhase.DEEP_DIVE)).isEqualTo(Duration.ofMinutes(25));
        assertThat(DesignClock.startOf(DesignPhase.BOTTLENECKS)).isEqualTo(Duration.ofMinutes(35));
    }

    @Test
    @DisplayName("the clock tells you where you should be")
    void phaseAtTime() {
        assertThat(DesignClock.phaseAt(Duration.ZERO)).contains(DesignPhase.SCOPE);
        assertThat(DesignClock.phaseAt(Duration.ofMinutes(4))).contains(DesignPhase.SCOPE);
        assertThat(DesignClock.phaseAt(Duration.ofMinutes(5))).contains(DesignPhase.ESTIMATE);
        assertThat(DesignClock.phaseAt(Duration.ofMinutes(20))).contains(DesignPhase.ARCHITECTURE);
        assertThat(DesignClock.phaseAt(Duration.ofMinutes(30))).contains(DesignPhase.DEEP_DIVE);
        assertThat(DesignClock.phaseAt(Duration.ofMinutes(44))).contains(DesignPhase.BOTTLENECKS);
    }

    @Test
    @DisplayName("at 45 minutes the round is over, whatever state the design is in")
    void timeIsUp() {
        assertThat(DesignClock.phaseAt(Duration.ofMinutes(45))).isEmpty();
        assertThat(DesignClock.phaseAt(Duration.ofMinutes(60))).isEmpty();
    }

    @Test
    @DisplayName("THE failure mode: still gathering requirements at minute 25")
    void detectsFallingBehind() {
        assertThat(DesignClock.isBehind(DesignPhase.SCOPE, Duration.ofMinutes(25)))
                .as("""
                        This is how most rounds are lost: not by running out of ideas, but by
                        running out of time in section one. Twenty-five minutes in and no boxes
                        drawn means the parts you are actually scored on never happen.""")
                .isTrue();

        assertThat(DesignClock.isBehind(DesignPhase.ARCHITECTURE, Duration.ofMinutes(20)))
                .as("exactly on plan")
                .isFalse();

        assertThat(DesignClock.isBehind(DesignPhase.DEEP_DIVE, Duration.ofMinutes(20)))
                .as("ahead of the clock is not behind it")
                .isFalse();
    }

    @Test
    @DisplayName("the deep dive and the bottlenecks get the most time - they carry the score")
    void weightingIsDeliberate() {
        assertThat(DesignPhase.DEEP_DIVE.budget()).isEqualTo(Duration.ofMinutes(10));
        assertThat(DesignPhase.BOTTLENECKS.budget()).isEqualTo(Duration.ofMinutes(10));

        assertThat(DesignPhase.SCOPE.budget())
                .as("scoping matters and is not where the marks are")
                .isLessThan(DesignPhase.DEEP_DIVE.budget());
    }

    @Test
    @DisplayName("every phase states what good looks like")
    void everyPhaseHasAGoal() {
        for (DesignPhase phase : DesignPhase.values()) {
            assertThat(phase.goal()).isNotBlank();
        }
    }
}
