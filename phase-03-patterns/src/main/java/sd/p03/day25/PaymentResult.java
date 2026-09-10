package sd.p03.day25;

/** The domain's own outcome shape - three variants, none of them a gateway status code. */
public sealed interface PaymentResult {

    record Approved(String confirmationId) implements PaymentResult {
    }

    record Declined(String reason) implements PaymentResult {
    }

    record GatewayError(String reason) implements PaymentResult {
    }
}
