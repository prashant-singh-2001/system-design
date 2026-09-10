package sd.p02.day18;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day18AggregateTest {

    private Order draftWithOneLine() {
        Order order = Order.draft("ord-1", "cust-1");
        order.addLine("SKU-A", 2, 1_500);
        return order;
    }

    @Test
    @DisplayName("a new order starts empty and in DRAFT")
    void startsAsDraft() {
        Order order = Order.draft("ord-1", "cust-1");

        assertThat(order.id()).isEqualTo("ord-1");
        assertThat(order.customerId()).isEqualTo("cust-1");
        assertThat(order.status()).isEqualTo(OrderStatus.DRAFT);
        assertThat(order.lines()).isEmpty();
        assertThat(order.totalCents()).isZero();
    }

    @Test
    @DisplayName("adding the same SKU merges quantities instead of duplicating the line")
    void addingSameSkuMerges() {
        Order order = Order.draft("ord-1", "cust-1");
        order.addLine("SKU-A", 2, 1_500);
        order.addLine("SKU-A", 3, 1_500);

        assertThat(order.lines()).hasSize(1);
        assertThat(order.lines().get(0).quantity()).isEqualTo(5);
        assertThat(order.totalCents()).isEqualTo(7_500);
    }

    @Test
    @DisplayName("bad line data is rejected")
    void validatesLineData() {
        Order order = Order.draft("ord-1", "cust-1");

        assertThatThrownBy(() -> order.addLine("SKU-A", 0, 100))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> order.addLine("SKU-A", -1, 100))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> order.addLine("SKU-A", 1, -100))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("the happy path: draft, submit, pay")
    void lifecycle() {
        Order order = draftWithOneLine();

        order.submit();
        assertThat(order.status()).isEqualTo(OrderStatus.SUBMITTED);

        order.pay();
        assertThat(order.status()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("an empty order cannot be submitted")
    void cannotSubmitEmpty() {
        assertThatThrownBy(() -> Order.draft("ord-1", "cust-1").submit())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("lines are frozen once the order is submitted")
    void cannotEditAfterSubmit() {
        Order order = draftWithOneLine();
        order.submit();

        assertThatThrownBy(() -> order.addLine("SKU-B", 1, 100))
                .as("the customer has committed - the basket is no longer theirs to change")
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> order.removeLine("SKU-A"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("a paid order cannot be cancelled - that is a refund, a different process")
    void cannotCancelPaidOrder() {
        Order order = draftWithOneLine();
        order.submit();
        order.pay();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("cancelling is allowed from DRAFT and from SUBMITTED")
    void cancellation() {
        Order fromDraft = draftWithOneLine();
        fromDraft.cancel();
        assertThat(fromDraft.status()).isEqualTo(OrderStatus.CANCELLED);

        Order fromSubmitted = draftWithOneLine();
        fromSubmitted.submit();
        fromSubmitted.cancel();
        assertThat(fromSubmitted.status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("only SUBMITTED orders can be paid")
    void cannotPayADraft() {
        assertThatThrownBy(() -> draftWithOneLine().pay())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("removing a line that is not there is a no-op, not a crash")
    void removingAbsentLine() {
        Order order = draftWithOneLine();
        order.removeLine("SKU-NOT-PRESENT");

        assertThat(order.lines()).hasSize(1);
    }

    @Test
    @DisplayName("the aggregate does not hand out mutable internals")
    void linesAreUnmodifiable() {
        Order order = draftWithOneLine();

        assertThatThrownBy(() -> order.lines().add(new OrderLine("SNEAK", 1, 1)))
                .as("an invariant you can bypass from outside is not an invariant")
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
