package sd.p03.day25;

/** One piece of the checkout subsystem the {@link PaymentFacade} hides behind one call. */
@FunctionalInterface
public interface FraudChecker {

    boolean isSuspicious(PaymentRequest request);
}
