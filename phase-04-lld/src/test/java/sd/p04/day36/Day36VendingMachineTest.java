package sd.p04.day36;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day36VendingMachineTest {

    private static VendingMachine freshMachine() {
        return new VendingMachine(List.of(
                new Item("A1", "Cola", 150, 2),
                new Item("A2", "Chips", 200, 0)));
    }

    @Test
    @DisplayName("selecting or refunding before any coins are inserted is a protocol violation")
    void protocolViolationsAreRejected() {
        VendingMachine machine = freshMachine();

        assertThatThrownBy(() -> machine.selectItem("A1")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(machine::refund).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("inserting coins moves the machine to HAS_COINS")
    void insertingCoinsChangesState() {
        VendingMachine machine = freshMachine();

        machine.insertCoin(100);

        assertThat(machine.state()).isEqualTo(MachineState.HAS_COINS);
    }

    @Test
    @DisplayName("inserting a non-positive amount is rejected")
    void nonPositiveCoinIsRejected() {
        VendingMachine machine = freshMachine();

        assertThatThrownBy(() -> machine.insertCoin(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> machine.insertCoin(-50)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("exact payment dispenses with zero change and returns to IDLE")
    void exactPaymentDispensesWithNoChange() {
        VendingMachine machine = freshMachine();
        machine.insertCoin(150);

        SelectionResult result = machine.selectItem("A1");

        assertThat(result).isEqualTo(new SelectionResult.Dispensed("Cola", 0));
        assertThat(machine.state()).isEqualTo(MachineState.IDLE);
    }

    @Test
    @DisplayName("overpayment dispenses with correct change")
    void overpaymentReturnsChange() {
        VendingMachine machine = freshMachine();
        machine.insertCoin(200);

        SelectionResult result = machine.selectItem("A1");

        assertThat(result).isEqualTo(new SelectionResult.Dispensed("Cola", 50));
    }

    @Test
    @DisplayName("insufficient funds is a business outcome, not a thrown exception")
    void insufficientFundsStaysInHasCoins() {
        VendingMachine machine = freshMachine();
        machine.insertCoin(50);

        SelectionResult result = machine.selectItem("A1");

        assertThat(result).isEqualTo(new SelectionResult.InsufficientFunds(100));
        assertThat(machine.state())
                .as("the customer should be able to add more coins and try again")
                .isEqualTo(MachineState.HAS_COINS);
    }

    @Test
    @DisplayName("an unknown code does not touch the balance")
    void unknownCodeDoesNotTouchBalance() {
        VendingMachine machine = freshMachine();
        machine.insertCoin(150);

        SelectionResult result = machine.selectItem("Z9");

        assertThat(result).isEqualTo(new SelectionResult.UnknownItem("Z9"));
        assertThat(machine.selectItem("A1"))
                .as("the original 150 must still be there for a valid selection")
                .isEqualTo(new SelectionResult.Dispensed("Cola", 0));
    }

    @Test
    @DisplayName("an empty slot reports out of stock without touching the balance")
    void outOfStockDoesNotTouchBalance() {
        VendingMachine machine = freshMachine();
        machine.insertCoin(200);

        SelectionResult result = machine.selectItem("A2");

        assertThat(result).isEqualTo(new SelectionResult.OutOfStock("A2"));
        assertThat(machine.state()).isEqualTo(MachineState.HAS_COINS);
    }

    @Test
    @DisplayName("refund returns the full balance and resets to IDLE")
    void refundReturnsBalance() {
        VendingMachine machine = freshMachine();
        machine.insertCoin(75);
        machine.insertCoin(25);

        long refunded = machine.refund();

        assertThat(refunded).isEqualTo(100);
        assertThat(machine.state()).isEqualTo(MachineState.IDLE);
        assertThatThrownBy(machine::refund)
                .as("nothing left to refund now that the machine is back to IDLE")
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("a dispensed item cannot be dispensed twice for one payment")
    void oneItemPerPayment() {
        VendingMachine machine = freshMachine();
        machine.insertCoin(150);
        machine.selectItem("A1");

        assertThatThrownBy(() -> machine.selectItem("A1"))
                .as("the machine is back in IDLE - no coins are on deposit any more")
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("stock actually decrements after a successful dispense")
    void stockDecrementsOnDispense() {
        VendingMachine machine = freshMachine();
        machine.insertCoin(150);
        machine.selectItem("A1");   // stock 2 -> 1

        machine.insertCoin(150);
        SelectionResult second = machine.selectItem("A1");   // stock 1 -> 0
        assertThat(second).isInstanceOf(SelectionResult.Dispensed.class);

        machine.insertCoin(150);
        assertThat(machine.selectItem("A1"))
                .as("the second cola was the last one")
                .isEqualTo(new SelectionResult.OutOfStock("A1"));
    }
}
