package sd.p03.day23;

/** A second, unrelated event type - used to prove the bus routes by TYPE, not by subscriber. */
public record OrderCancelled(String orderId) implements Event {
}
