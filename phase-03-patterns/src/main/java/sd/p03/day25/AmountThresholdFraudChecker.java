package sd.p03.day25;

/** GIVEN - the simplest possible rule: anything over the threshold gets a second look. */
public final class AmountThresholdFraudChecker implements FraudChecker {

    private final long thresholdCents;

    public AmountThresholdFraudChecker(long thresholdCents) {
        this.thresholdCents = thresholdCents;
    }

    @Override
    public boolean isSuspicious(PaymentRequest request) {
        return request.amountCents() > thresholdCents;
    }
}
