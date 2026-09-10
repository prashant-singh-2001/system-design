package sd.p03.day23;

/** A sample event - the SUBJECT in Observer terms notifies of exactly this kind of occurrence. */
public record OrderPlaced(String orderId, long totalCents) implements Event {
}
