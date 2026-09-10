package sd.p04.day37;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day37SplitwiseTest {

    private final ExpenseSplitter splitter = new ExpenseSplitter();

    @Test
    @DisplayName("EQUAL split: the remainder goes to the first participants, and it always sums exactly")
    void equalSplitDistributesRemainder() {
        Map<String, Long> shares = splitter.splitEqually(100, List.of("alice", "bob", "carol"));

        assertThat(shares).isEqualTo(Map.of("alice", 34L, "bob", 33L, "carol", 33L));
        assertThat(shares.values().stream().mapToLong(Long::longValue).sum()).isEqualTo(100);
    }

    @Test
    @DisplayName("EXACT split: amounts that do not sum to the total are rejected")
    void exactSplitValidatesTotal() {
        Map<String, Long> bad = Map.of("alice", 40L, "bob", 40L);

        assertThatThrownBy(() -> splitter.splitExactly(bad, 100))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("EXACT split: correct amounts pass through unchanged")
    void exactSplitPassesThroughValidAmounts() {
        Map<String, Long> exact = Map.of("alice", 60L, "bob", 40L);

        assertThat(splitter.splitExactly(exact, 100)).isEqualTo(exact);
    }

    @Test
    @DisplayName("PERCENTAGE split: shares sum exactly to the total after rounding is fixed up")
    void percentageSplitSumsExactly() {
        Map<String, Double> percentages = new LinkedHashMap<>();
        percentages.put("alice", 50.0);
        percentages.put("bob", 25.0);
        percentages.put("carol", 25.0);

        Map<String, Long> shares = splitter.splitByPercentage(percentages, 101);

        assertThat(shares).isEqualTo(Map.of("alice", 51L, "bob", 25L, "carol", 25L));
        assertThat(shares.values().stream().mapToLong(Long::longValue).sum()).isEqualTo(101);
    }

    @Test
    @DisplayName("PERCENTAGE split: percentages that do not sum to 100 are rejected")
    void percentageSplitValidatesSum() {
        Map<String, Double> bad = Map.of("alice", 50.0, "bob", 40.0);

        assertThatThrownBy(() -> splitter.splitByPercentage(bad, 100))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("LEDGER: paying for a group credits the payer with what the others owe")
    void ledgerRecordsAnEqualExpense() {
        Ledger ledger = new Ledger();
        Map<String, Long> shares = splitter.splitEqually(90, List.of("alice", "bob", "carol"));

        ledger.recordExpense("alice", shares);

        assertThat(ledger.balanceOf("alice"))
                .as("alice covered 90 but her own fair share was 30, so she is owed 60 back")
                .isEqualTo(60);
        assertThat(ledger.balanceOf("bob")).isEqualTo(-30);
        assertThat(ledger.balanceOf("carol")).isEqualTo(-30);
    }

    @Test
    @DisplayName("LEDGER: balances accumulate correctly across multiple expenses")
    void ledgerAccumulatesAcrossExpenses() {
        Ledger ledger = new Ledger();
        ledger.recordExpense("alice", splitter.splitEqually(90, List.of("alice", "bob", "carol")));
        ledger.recordExpense("bob", splitter.splitEqually(30, List.of("alice", "bob")));

        // First expense: alice +60, bob -30, carol -30.
        // Second expense: bob pays 30, split alice/bob 15 each -> bob +15, alice -15.
        assertThat(ledger.balanceOf("alice")).isEqualTo(45);
        assertThat(ledger.balanceOf("bob")).isEqualTo(-15);
        assertThat(ledger.balanceOf("carol")).isEqualTo(-30);
    }

    @Test
    @DisplayName("LEDGER: an unmentioned person has a zero balance")
    void unmentionedPersonHasZeroBalance() {
        assertThat(new Ledger().balanceOf("nobody")).isZero();
    }

    @Test
    @DisplayName("SETTLEMENT: a simple two-person debt settles in one transfer")
    void settlesSimpleTwoPersonDebt() {
        List<Transfer> transfers = new SettlementCalculator().settle(Map.of("alice", 60L, "bob", -60L));

        assertThat(transfers).containsExactly(new Transfer("bob", "alice", 60));
    }

    @Test
    @DisplayName("SETTLEMENT: balances that do not sum to zero are rejected")
    void settlementRejectsUnbalancedInput() {
        assertThatThrownBy(() -> new SettlementCalculator().settle(Map.of("alice", 60L, "bob", -50L)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("SETTLEMENT: a three-way case nets out correctly with at most n-1 transfers")
    void settlesThreeWayCaseCorrectly() {
        Map<String, Long> balances = Map.of("alice", -30L, "bob", 10L, "carol", 20L);

        List<Transfer> transfers = new SettlementCalculator().settle(balances);

        assertThat(transfers.size())
                .as("at most (participants - 1) transfers for the greedy min-cash-flow approach")
                .isLessThanOrEqualTo(2);

        Map<String, Long> netEffect = new HashMap<>();
        for (Transfer transfer : transfers) {
            netEffect.merge(transfer.from(), -transfer.amountCents(), Long::sum);
            netEffect.merge(transfer.to(), transfer.amountCents(), Long::sum);
        }
        balances.forEach((person, balance) ->
                assertThat(netEffect.getOrDefault(person, 0L))
                        .as("the transfers must reproduce %s's original balance exactly", person)
                        .isEqualTo(balance));
    }

    @Test
    @DisplayName("SETTLEMENT: someone with a zero balance appears in no transfer")
    void zeroBalancePersonIsExcluded() {
        Map<String, Long> balances = Map.of("alice", 50L, "bob", -50L, "carol", 0L);

        List<Transfer> transfers = new SettlementCalculator().settle(balances);

        assertThat(transfers).noneMatch(t -> t.from().equals("carol") || t.to().equals("carol"));
    }
}
