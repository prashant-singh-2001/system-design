package sd.p03.day25;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

class Day25AdapterFacadeTest {

    private static PaymentRequest request(long amountCents) {
        return new PaymentRequest("4111111111111111", 3, 2027, amountCents, "USD");
    }

    @Test
    @DisplayName("a status-0 charge becomes Approved with the gateway's transaction id")
    void approvedTranslation() {
        LegacyPaymentGatewayApi api = new LegacyPaymentGatewayApi(0, "txn-123", null);
        PaymentProcessor adapter = new LegacyPaymentGatewayAdapter(api);

        PaymentResult result = adapter.charge(request(5_000));

        assertThat(result).isEqualTo(new PaymentResult.Approved("txn-123"));
    }

    @Test
    @DisplayName("a status-1 charge becomes Declined with the gateway's error detail")
    void declinedTranslation() {
        LegacyPaymentGatewayApi api = new LegacyPaymentGatewayApi(1, null, "insufficient funds");
        PaymentProcessor adapter = new LegacyPaymentGatewayAdapter(api);

        PaymentResult result = adapter.charge(request(5_000));

        assertThat(result).isEqualTo(new PaymentResult.Declined("insufficient funds"));
    }

    @Test
    @DisplayName("a status-2 charge becomes a GatewayError")
    void errorTranslation() {
        LegacyPaymentGatewayApi api = new LegacyPaymentGatewayApi(2, null, "gateway timeout");
        PaymentProcessor adapter = new LegacyPaymentGatewayAdapter(api);

        PaymentResult result = adapter.charge(request(5_000));

        assertThat(result).isEqualTo(new PaymentResult.GatewayError("gateway timeout"));
    }

    @Test
    @DisplayName("cents are converted to dollars exactly, on the way into the hostile API")
    void amountConversion() {
        LegacyPaymentGatewayApi api = new LegacyPaymentGatewayApi(0, "txn-1", null);
        PaymentProcessor adapter = new LegacyPaymentGatewayAdapter(api);

        adapter.charge(request(12_345));

        assertThat(api.recordedAmountDollars()).isCloseTo(123.45, offset(0.0001));
    }

    @Test
    @DisplayName("expiry month and year are packed into zero-padded MMYY")
    void expiryFormatting() {
        LegacyPaymentGatewayApi api = new LegacyPaymentGatewayApi(0, "txn-1", null);
        PaymentProcessor adapter = new LegacyPaymentGatewayAdapter(api);

        adapter.charge(new PaymentRequest("4111111111111111", 3, 2027, 1_000, "USD"));
        assertThat(api.recordedExpiryMMYY()).isEqualTo("0327");

        adapter.charge(new PaymentRequest("4111111111111111", 12, 2030, 1_000, "USD"));
        assertThat(api.recordedExpiryMMYY()).isEqualTo("1230");
    }

    @Test
    @DisplayName("FACADE: a suspicious amount is blocked before the gateway is ever called")
    void facadeBlocksSuspiciousAmountsBeforeCharging() {
        int[] chargeCalls = {0};
        PaymentProcessor countingProcessor = req -> {
            chargeCalls[0]++;
            return new PaymentResult.Approved("txn-should-not-happen");
        };
        PaymentFacade facade = new PaymentFacade(
                new AmountThresholdFraudChecker(1_000_00), countingProcessor, new ReceiptService());

        PaymentResult result = facade.purchase("ord-1", request(2_000_00));

        assertThat(result).isEqualTo(new PaymentResult.Declined("blocked for fraud review"));
        assertThat(chargeCalls[0]).as("the processor must never be reached").isZero();
    }

    @Test
    @DisplayName("FACADE: an approved purchase records exactly one receipt")
    void facadeRecordsReceiptOnApproval() {
        ReceiptService receipts = new ReceiptService();
        PaymentProcessor alwaysApproves = req -> new PaymentResult.Approved("txn-999");
        PaymentFacade facade = new PaymentFacade(
                new AmountThresholdFraudChecker(1_000_00), alwaysApproves, receipts);

        facade.purchase("ord-1", request(5_000));

        assertThat(receipts.all()).containsExactly(new Receipt("ord-1", "txn-999", 5_000));
    }

    @Test
    @DisplayName("FACADE: a declined purchase records no receipt")
    void facadeRecordsNoReceiptOnDecline() {
        ReceiptService receipts = new ReceiptService();
        PaymentProcessor alwaysDeclines = req -> new PaymentResult.Declined("card expired");
        PaymentFacade facade = new PaymentFacade(
                new AmountThresholdFraudChecker(1_000_00), alwaysDeclines, receipts);

        facade.purchase("ord-1", request(5_000));

        assertThat(receipts.all()).isEmpty();
    }
}
