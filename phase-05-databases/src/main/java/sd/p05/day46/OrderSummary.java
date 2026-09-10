package sd.p05.day46;

/** One order, reduced to exactly what a profile view needs to show about it. */
public record OrderSummary(long orderId, long totalCents) {
}
