package sd.p03.day25;

/**
 * TODO(day25): the FACADE - one call, {@link #purchase}, standing in front of three subsystems
 * ({@link FraudChecker}, {@link PaymentProcessor}, {@link ReceiptService}) that a caller should
 * never have to sequence by hand.
 *
 * <p>{@code purchase(orderId, request)}:
 * <ol>
 *   <li>if {@code fraudChecker.isSuspicious(request)}, return
 *       {@code new PaymentResult.Declined("blocked for fraud review")} WITHOUT calling the
 *       processor at all - a suspicious transaction should never reach the gateway, let alone
 *       be charged a real card processing fee for the privilege of being declined.</li>
 *   <li>otherwise, call {@code processor.charge(request)}</li>
 *   <li>if the result is {@code Approved}, record a {@link Receipt} via the {@link
 *       ReceiptService} - {@code orderId}, the confirmation id, and the charged amount</li>
 *   <li>return whatever the processor returned</li>
 * </ol>
 *
 * <p>Facade is not Adapter wearing a different hat: an adapter translates ONE hostile interface;
 * a facade SEQUENCES several already-reasonable ones into the one call a caller actually wants.
 * You are using both today because that is exactly how they show up together in a real checkout
 * path.
 */
public final class PaymentFacade {

    public PaymentFacade(FraudChecker fraudChecker, PaymentProcessor processor,
                          ReceiptService receipts) {
        throw new UnsupportedOperationException("TODO(day25): store the three collaborators");
    }

    public PaymentResult purchase(String orderId, PaymentRequest request) {
        throw new UnsupportedOperationException(
                "TODO(day25): fraud check, charge, record a receipt on approval");
    }
}
