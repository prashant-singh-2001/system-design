package sd.p03.day29;

/** Everything that can travel through a worker's queue: real work, or the signal to stop. */
public sealed interface WorkItem permits Job, PoisonPill {
}
