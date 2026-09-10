package sd.p03.day25;

/** GIVEN - what gets kept once a purchase actually goes through. */
public record Receipt(String orderId, String confirmationId, long amountCents) {
}
