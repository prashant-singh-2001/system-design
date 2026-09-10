package sd.p04.day37;

import java.util.List;
import java.util.Map;

/**
 * TODO(day37): "optimal account balancing" - turn a set of net balances into a SMALL list of
 * transfers that settles everyone, instead of the naive approach of settling every pairwise IOU
 * that led to those balances (which can be many more transfers than are actually necessary once
 * debts have netted against each other).
 *
 * <p>{@code settle(balances)} must reject a set of balances that does not sum to zero -
 * {@code IllegalArgumentException} - because money cannot appear or disappear; every cent owed
 * by someone must be owed TO someone else in the same set.
 *
 * <p>A simple GREEDY algorithm is enough: repeatedly find the person owed the MOST (the largest
 * positive balance) and the person owing the MOST (the largest negative balance, i.e. most
 * negative), transfer {@code min(largest credit, largest debt)} between them, and reduce both
 * balances by that amount. Repeat until every remaining balance is zero. A person whose balance
 * is already zero never appears in the output.
 *
 * <p>This will not always produce the mathematically minimum number of transfers for every
 * possible input (that is a harder subset-sum-flavoured problem), but it is a substantial,
 * real improvement over "everyone pays back exactly who they individually owed," and it is what
 * most real expense-splitting apps actually ship.
 */
public final class SettlementCalculator {

    public List<Transfer> settle(Map<String, Long> balances) {
        throw new UnsupportedOperationException(
                "TODO(day37): validate the sum is zero, then greedily match largest creditor/debtor");
    }
}
