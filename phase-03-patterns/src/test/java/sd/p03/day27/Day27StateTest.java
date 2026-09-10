package sd.p03.day27;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day27StateTest {

    @Test
    @DisplayName("a new lifecycle starts in DRAFT")
    void startsInDraft() {
        assertThat(new OrderLifecycle().status()).isEqualTo("DRAFT");
    }

    @Test
    @DisplayName("submitting with no lines is refused, by the default template method")
    void submitWithoutLinesFails() {
        assertThatThrownBy(() -> new OrderLifecycle().submit())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no lines");
    }

    @Test
    @DisplayName("submit moves DRAFT to SUBMITTED once there is at least one line")
    void submitMovesToSubmitted() {
        OrderLifecycle order = new OrderLifecycle();
        order.addLine();

        order.submit();

        assertThat(order.status()).isEqualTo("SUBMITTED");
    }

    @Test
    @DisplayName("pay moves SUBMITTED to PAID")
    void payMovesToPaid() {
        OrderLifecycle order = new OrderLifecycle();
        order.addLine();
        order.submit();

        order.pay();

        assertThat(order.status()).isEqualTo("PAID");
    }

    @Test
    @DisplayName("pay from DRAFT is refused by the inherited default - no override needed")
    void payFromDraftFails() {
        assertThatThrownBy(() -> new OrderLifecycle().pay())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("cannot pay from DRAFT");
    }

    @Test
    @DisplayName("cancel works from DRAFT or SUBMITTED")
    void cancelWorksFromDraftOrSubmitted() {
        OrderLifecycle fromDraft = new OrderLifecycle();
        fromDraft.cancel();
        assertThat(fromDraft.status()).isEqualTo("CANCELLED");

        OrderLifecycle fromSubmitted = new OrderLifecycle();
        fromSubmitted.addLine();
        fromSubmitted.submit();
        fromSubmitted.cancel();
        assertThat(fromSubmitted.status()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("a paid order cannot be cancelled - PaidState overrides nothing, on purpose")
    void cancelFromPaidFails() {
        OrderLifecycle order = new OrderLifecycle();
        order.addLine();
        order.submit();
        order.pay();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("cannot cancel from PAID");
    }

    @Test
    @DisplayName("CANCELLED is genuinely terminal - nothing is legal from it")
    void cancelledIsTerminal() {
        OrderLifecycle order = new OrderLifecycle();
        order.cancel();

        assertThatThrownBy(order::submit).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(order::pay).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(order::cancel).isInstanceOf(IllegalStateException.class);
    }
}
