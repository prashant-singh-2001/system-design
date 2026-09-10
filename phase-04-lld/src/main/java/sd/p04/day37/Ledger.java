package sd.p04.day37;

import java.util.Map;

/**
 * TODO(day37): the running net balance between every person in the group, built up one expense
 * at a time. Positive means "is owed money overall"; negative means "owes money overall".
 *
 * <p>{@code recordExpense(paidBy, owedByEachCents)}: {@code owedByEachCents} is one of {@link
 * ExpenseSplitter}'s outputs - each participant's share of ONE expense. For every participant
 * OTHER than {@code paidBy}, that participant's balance goes DOWN by their share (they owe it),
 * and {@code paidBy}'s balance goes UP by that same amount (they are owed it) - {@code paidBy}'s
 * own share nets out to zero against themselves, so it never needs special-casing.
 *
 * <p>{@code balanceOf(person)}: 0 for anyone never mentioned in an expense.
 */
public final class Ledger {

    public void recordExpense(String paidBy, Map<String, Long> owedByEachCents) {
        throw new UnsupportedOperationException(
                "TODO(day37): move each non-payer's share from them to paidBy");
    }

    public long balanceOf(String person) {
        throw new UnsupportedOperationException("TODO(day37): 0 if never seen, else the running total");
    }

    public Map<String, Long> balances() {
        throw new UnsupportedOperationException("TODO(day37): return every known person's balance");
    }
}
