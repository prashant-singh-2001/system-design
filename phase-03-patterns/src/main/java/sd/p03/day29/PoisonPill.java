package sd.p03.day29;

/**
 * The classic PRODUCER-CONSUMER shutdown signal: a value indistinguishable from real work at the
 * type-and-queue level, except that a worker who takes one knows to stop instead of process.
 * Carries no data - its entire meaning is "you are done, exit your loop".
 */
public record PoisonPill() implements WorkItem {
}
