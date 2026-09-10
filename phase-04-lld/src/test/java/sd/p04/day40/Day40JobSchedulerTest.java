package sd.p04.day40;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class Day40JobSchedulerTest {

    @Test
    @DisplayName("a job scheduled in the future does not run before its time")
    void jobDoesNotRunBeforeDue() {
        TestClock clock = new TestClock();
        JobScheduler scheduler = new JobScheduler(clock);
        List<String> ran = new ArrayList<>();
        scheduler.schedule(clock.now().plus(Duration.ofMinutes(10)), () -> ran.add("job"));

        int count = scheduler.runDueJobs();

        assertThat(count).isZero();
        assertThat(ran).isEmpty();
        assertThat(scheduler.pendingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("a job runs exactly once, once its time has arrived")
    void jobRunsOnceDue() {
        TestClock clock = new TestClock();
        JobScheduler scheduler = new JobScheduler(clock);
        List<String> ran = new ArrayList<>();
        scheduler.schedule(clock.now().plus(Duration.ofMinutes(10)), () -> ran.add("job"));

        clock.advance(Duration.ofMinutes(10));
        int firstSweep = scheduler.runDueJobs();
        int secondSweep = scheduler.runDueJobs();

        assertThat(firstSweep).isEqualTo(1);
        assertThat(secondSweep).as("the job must not run again on a later sweep").isZero();
        assertThat(ran).containsExactly("job");
        assertThat(scheduler.pendingCount()).isZero();
    }

    @Test
    @DisplayName("multiple due jobs run in one sweep, earliest runAt first")
    void multipleDueJobsRunInOrder() {
        TestClock clock = new TestClock();
        JobScheduler scheduler = new JobScheduler(clock);
        List<String> order = new ArrayList<>();
        scheduler.schedule(clock.now().plus(Duration.ofMinutes(5)), () -> order.add("second"));
        scheduler.schedule(clock.now().plus(Duration.ofMinutes(1)), () -> order.add("first"));

        clock.advance(Duration.ofMinutes(10));
        int count = scheduler.runDueJobs();

        assertThat(count).isEqualTo(2);
        assertThat(order).containsExactly("first", "second");
    }

    @Test
    @DisplayName("cancelling a pending job stops it from ever running")
    void cancelPreventsExecution() {
        TestClock clock = new TestClock();
        JobScheduler scheduler = new JobScheduler(clock);
        List<String> ran = new ArrayList<>();
        String jobId = scheduler.schedule(clock.now().plus(Duration.ofMinutes(1)), () -> ran.add("job"));

        boolean cancelled = scheduler.cancel(jobId);
        clock.advance(Duration.ofMinutes(5));
        scheduler.runDueJobs();

        assertThat(cancelled).isTrue();
        assertThat(ran).isEmpty();
    }

    @Test
    @DisplayName("cancelling an unknown or already-handled job id returns false")
    void cancelUnknownJobReturnsFalse() {
        TestClock clock = new TestClock();
        JobScheduler scheduler = new JobScheduler(clock);

        assertThat(scheduler.cancel("no-such-job")).isFalse();

        String jobId = scheduler.schedule(clock.now(), () -> { });
        scheduler.runDueJobs();
        assertThat(scheduler.cancel(jobId)).as("a job that already ran cannot be cancelled").isFalse();
    }

    @Test
    @DisplayName("one job throwing does not stop the rest of the sweep from running")
    void aFailingJobDoesNotBlockOthers() {
        TestClock clock = new TestClock();
        JobScheduler scheduler = new JobScheduler(clock);
        List<String> ran = new ArrayList<>();
        scheduler.schedule(clock.now(), () -> {
            throw new RuntimeException("boom");
        });
        scheduler.schedule(clock.now(), () -> ran.add("survivor"));

        int count = scheduler.runDueJobs();

        assertThat(ran).containsExactly("survivor");
        assertThat(count)
                .as("both jobs were due and attempted, whether or not one of them threw")
                .isEqualTo(2);
    }

    @Test
    @DisplayName("jobs due at the exact same instant both run in the same sweep")
    void simultaneousJobsBothRun() {
        TestClock clock = new TestClock();
        JobScheduler scheduler = new JobScheduler(clock);
        List<String> ran = new ArrayList<>();
        scheduler.schedule(clock.now(), () -> ran.add("a"));
        scheduler.schedule(clock.now(), () -> ran.add("b"));

        int count = scheduler.runDueJobs();

        assertThat(count).isEqualTo(2);
        assertThat(ran).containsExactlyInAnyOrder("a", "b");
    }
}
