package sd.p04.day40;

import java.time.Instant;

/** One entry in the scheduler's pending set: when it should run, and what it does. */
record ScheduledJob(String id, Instant runAt, Runnable task) {
}
