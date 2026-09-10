package sd.p03.day29;

/** One unit of real work. */
public record Job(String id, int payload) implements WorkItem {
}
