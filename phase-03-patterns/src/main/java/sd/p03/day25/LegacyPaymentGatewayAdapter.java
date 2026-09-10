package sd.p03.day25;

/**
 * TODO(day25): the ADAPTER - an anti-corruption layer between the domain's {@link
 * PaymentProcessor} port and {@link LegacyPaymentGatewayApi}'s hostile shape.
 *
 * <p>{@code charge(request)}:
 * <ol>
 *   <li>convert {@code amountCents} to dollars: {@code amountCents / 100.0}</li>
 *   <li>format the expiry as {@code "MMYY"}, zero-padded to two digits each - month 3, year
 *       2027 becomes {@code "0327"}, not {@code "327"} or {@code "3027"}</li>
 *   <li>call {@code api.chargeCard(cardNumber, expiryMMYY, amountDollars)}</li>
 *   <li>{@code 0} -&gt; {@code new PaymentResult.Approved(api.lastTransactionId())}</li>
 *   <li>{@code 1} -&gt; {@code new PaymentResult.Declined(api.lastErrorMessage())}</li>
 *   <li>{@code 2}, or anything else the gateway might one day return -&gt;
 *       {@code new PaymentResult.GatewayError(...)}</li>
 * </ol>
 *
 * <p>Notice everything this class hides: nobody calling {@link PaymentProcessor#charge} ever
 * touches a double, a two-digit year string, or a status code. If the gateway vendor changes
 * their SDK next year, this is the only file that moves.
 */
public final class LegacyPaymentGatewayAdapter implements PaymentProcessor {

    public LegacyPaymentGatewayAdapter(LegacyPaymentGatewayApi api) {
        throw new UnsupportedOperationException("TODO(day25): store the wrapped API");
    }

    @Override
    public PaymentResult charge(PaymentRequest request) {
        throw new UnsupportedOperationException(
                "TODO(day25): translate request -> legacy call -> PaymentResult");
    }
}
