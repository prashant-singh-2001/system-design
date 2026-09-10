package sd.p03.day25;

/** The domain's own shape for a charge request - nothing here knows the gateway exists. */
public record PaymentRequest(String cardNumber, int expiryMonth, int expiryYear,
                              long amountCents, String currency) {
}
