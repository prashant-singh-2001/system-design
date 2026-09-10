package sd.p08.day74;

public record PaymentResult(String paymentId, String orderId, long amountCents, boolean replayed) {

    public PaymentResult asReplay() {
        return new PaymentResult(paymentId, orderId, amountCents, true);
    }
}
