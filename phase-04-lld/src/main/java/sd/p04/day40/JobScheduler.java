package sd.p04.day40;

import java.time.Clock;
import java.time.Instant;

/**
 * TODO(day40) - THE TIMED ROUND. Do not open this file until you have read the brief and run
 * your own clock for the requirements/entities/API sections of {@code lld-template.md} first.
 * This class and its test are the "Build" step you check yourself against afterward - not a
 * hint to read before you have written your own version of the plan.
 *
 * <p>A job scheduler that runs jobs at (or after) a specific {@link Instant}, without real
 * threads or real sleeping - {@code runDueJobs()} is called explicitly, exactly like Day 35's
 * {@code Elevator.step()} advanced simulated time one unit at a time instead of waiting on a
 * real clock.
 *
 * <p>{@code schedule(runAt, task)}: generate and return a new job id; remember the job as
 * pending, keyed by that id.
 *
 * <p>{@code cancel(jobId)}: remove a PENDING job so it never runs; return {@code true} if
 * something was actually removed, {@code false} for an unknown id or a job that already ran.
 *
 * <p>{@code runDueJobs()}: run every pending job whose {@code runAt} is at or before
 * {@code clock.instant()}, in {@code runAt} order (earliest first), removing each from the
 * pending set as it runs. If a job's {@code task.run()} throws, catch it - do not let one bad
 * job stop the rest of this sweep, the same isolation Day 29's worker pool applied to a
 * queue instead of a clock. Return how many due jobs were ATTEMPTED this sweep - a thrown
 * exception still counts as an attempt, since the job was due and the scheduler did try it.
 *
 * <p>{@code pendingCount()}: how many jobs are scheduled and have neither run nor been
 * cancelled.
 */
public final class JobScheduler {

    public JobScheduler(Clock clock) {
        throw new UnsupportedOperationException("TODO(day40): store the clock, init the pending set");
    }

    public String schedule(Instant runAt, Runnable task) {
        throw new UnsupportedOperationException("TODO(day40): generate an id, record the pending job");
    }

    public boolean cancel(String jobId) {
        throw new UnsupportedOperationException("TODO(day40): remove if pending, report whether it was");
    }

    public int runDueJobs() {
        throw new UnsupportedOperationException(
                "TODO(day40): run every due job in runAt order, isolating failures, return the count");
    }

    public int pendingCount() {
        throw new UnsupportedOperationException("TODO(day40): implement pendingCount");
    }
}
