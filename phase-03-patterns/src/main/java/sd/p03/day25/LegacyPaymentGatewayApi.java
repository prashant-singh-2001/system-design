package sd.p03.day25;

/**
 * GIVEN - a stand-in for a hostile third-party SDK, hostile in every one of the usual ways:
 *
 * <ul>
 *   <li>amounts are {@code double} DOLLARS, not integer minor units - the exact anti-pattern
 *       Day 16 warned about, arriving here from outside your control</li>
 *   <li>a card's expiry is one string, {@code "MMYY"}, instead of two typed fields</li>
 *   <li>success or failure is a bare status CODE ({@code 0}/{@code 1}/{@code 2}), not a type</li>
 *   <li>the actual error detail is not returned - it is left in mutable state on the object,
 *       retrieved by a SEPARATE call that only means something right after a failed charge</li>
 * </ul>
 *
 * <p>Nothing about this class is unrealistic - most real payment SDKs look uncomfortably close
 * to this. The job is never to fix the third party's API. It is to build one adapter that
 * absorbs all four of these smells in one place, so nothing else in the codebase ever has to
 * know they exist.
 *
 * <p>This particular fake is scripted rather than simulating real card logic: it always returns
 * the status and detail it was constructed with, and remembers exactly what it was called with,
 * so a test can both drive a scenario and verify the adapter's translation in both directions.
 */
public final class LegacyPaymentGatewayApi {

    private final int statusToReturn;
    private final String transactionId;
    private final String errorMessage;

    private String recordedCardNumber;
    private String recordedExpiryMMYY;
    private double recordedAmountDollars;

    public LegacyPaymentGatewayApi(int statusToReturn, String transactionId, String errorMessage) {
        this.statusToReturn = statusToReturn;
        this.transactionId = transactionId;
        this.errorMessage = errorMessage;
    }

    /** 0 = approved, 1 = declined, 2 = error. Amount is in DOLLARS. Expiry is "MMYY". */
    public int chargeCard(String cardNumber, String expiryMMYY, double amountDollars) {
        this.recordedCardNumber = cardNumber;
        this.recordedExpiryMMYY = expiryMMYY;
        this.recordedAmountDollars = amountDollars;
        return statusToReturn;
    }

    /** Only meaningful after a call that returned {@code 0}. */
    public String lastTransactionId() {
        return transactionId;
    }

    /** Only meaningful after a call that returned {@code 1} or {@code 2}. */
    public String lastErrorMessage() {
        return errorMessage;
    }

    public String recordedCardNumber() {
        return recordedCardNumber;
    }

    public String recordedExpiryMMYY() {
        return recordedExpiryMMYY;
    }

    public double recordedAmountDollars() {
        return recordedAmountDollars;
    }
}
