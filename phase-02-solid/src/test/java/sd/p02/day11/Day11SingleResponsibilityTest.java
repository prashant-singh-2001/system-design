package sd.p02.day11;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day11SingleResponsibilityTest {

    // --- test doubles: the whole point is that these are trivial to write ---

    private static final class RecordingRepository implements OrderRepository {
        final List<String> saved = new ArrayList<>();

        @Override
        public void save(Order order, long totalCents) {
            saved.add(order.id() + ":" + totalCents);
        }
    }

    private static final class RecordingSender implements ConfirmationSender {
        final List<String> sent = new ArrayList<>();

        @Override
        public void sendConfirmation(String customerEmail, String orderId, long totalCents) {
            sent.add(customerEmail + "/" + orderId + "/" + totalCents);
        }
    }

    @Test
    @DisplayName("pricing is testable with no database and no email server")
    void pricingInIsolation() {
        PricingService pricing = new PricingService();
        Order order = Order.sample();               // 2 x 1500 + 1 x 3000 = 6000

        assertThat(pricing.subtotalCents(order)).isEqualTo(6_000);
        assertThat(pricing.discountedSubtotalCents(order)).isEqualTo(6_000);
        // 6000 + 1200 tax + 0 shipping (free at >= 5000)
        assertThat(pricing.totalCents(order)).isEqualTo(7_200);
    }

    @Test
    @DisplayName("the coupon and the shipping threshold still behave exactly as before")
    void pricingEdgeCases() {
        PricingService pricing = new PricingService();

        Order discounted = new Order("ord-2", "a@b.com",
                List.of(new OrderLine("SKU-A", 1, 6_000)), "SAVE10");
        // 6000 - 600 = 5400, tax 1080, shipping free -> 6480
        assertThat(pricing.totalCents(discounted)).isEqualTo(6_480);

        Order small = new Order("ord-3", "a@b.com",
                List.of(new OrderLine("SKU-C", 1, 1_000)), null);
        // 1000 + 200 tax + 499 shipping -> 1699
        assertThat(pricing.totalCents(small)).isEqualTo(1_699);
    }

    @Test
    @DisplayName("validation is testable on its own too")
    void validationInIsolation() {
        OrderValidator validator = new OrderValidator();

        assertThatThrownBy(() -> validator.validate(
                new Order("", "a@b.com", List.of(new OrderLine("X", 1, 1)), null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("order id is required");

        assertThatThrownBy(() -> validator.validate(
                new Order("ord", "not-an-email", List.of(new OrderLine("X", 1, 1)), null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("valid customer email is required");

        assertThatThrownBy(() -> validator.validate(new Order("ord", "a@b.com", List.of(), null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("order must have at least one line");

        assertThatThrownBy(() -> validator.validate(
                new Order("ord", "a@b.com", List.of(new OrderLine("X", 0, 1)), null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("quantity must be positive: X");
    }

    @Test
    @DisplayName("the coordinator orchestrates and nothing more")
    void processorCoordinates() {
        RecordingRepository repository = new RecordingRepository();
        RecordingSender sender = new RecordingSender();
        OrderProcessor processor = new OrderProcessor(
                new OrderValidator(), new PricingService(), repository, sender);

        long total = processor.process(Order.sample());

        assertThat(total).isEqualTo(7_200);
        assertThat(repository.saved).containsExactly("ord-1:7200");
        assertThat(sender.sent).containsExactly("buyer@example.com/ord-1/7200");
    }

    @Test
    @DisplayName("behaviour is preserved - the refactored total matches the legacy total")
    void refactoringPreservesBehaviour() {
        Order order = Order.sample();

        long legacyTotal = new LegacyOrderProcessor().process(order);
        long refactoredTotal = new OrderProcessor(new OrderValidator(), new PricingService(),
                (o, t) -> { }, (e, o, t) -> { }).process(order);

        assertThat(refactoredTotal)
                .as("a refactor changes structure, never behaviour")
                .isEqualTo(legacyTotal);
    }
}
