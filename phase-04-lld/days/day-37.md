# Day 37 - Modelling money, debt and settlement

**Phase 4 - Low-level design craft** | 45 minutes

## Concept (10 min)

Splitwise's actual hard part is not the UI - it is three arithmetic problems that all share one
invariant Day 16 already taught you: **shares must sum to EXACTLY the total, in minor units,
with no cent created or destroyed.** Equal splits, percentage splits and the ledger's running
balances are all just `Money.allocate` wearing different clothes.

**Splitting** has three flavours - equal (divide and distribute the remainder), exact (the
caller states each share; validate it sums correctly), percentage (validate the percentages sum
to ~100%, then split-and-fix-up rounding exactly like the equal case). Get the remainder handling
wrong in any of them and you have built the "where did the penny go" bug in a system whose entire
job is tracking pennies.

**The ledger** turns a sequence of "X paid, split like THIS among these people" events into a
running net balance per person - positive means owed money, negative means owing it. The key
insight: a payer's own theoretical share nets against themselves automatically the moment you
only move OTHER participants' shares onto the payer's balance, so it never needs special-casing.

**Settlement** is where this stops being arithmetic and becomes an actual algorithm: given a set
of net balances, produce a SMALL number of transfers that settles everyone, rather than
literally replaying every pairwise IOU that produced those balances. A simple GREEDY approach -
repeatedly match the largest creditor with the largest debtor - is genuinely most of what real
expense-splitting apps ship, and it is a satisfying algorithm to build once by hand.

## Build (25 min)

In `src/main/java/sd/p04/day37/`: `ExpenseSplitter` (equal, exact, percentage - each summing
exactly), `Ledger` (`recordExpense`, `balanceOf`), and `SettlementCalculator` (validate balances
sum to zero, then greedily match largest creditor against largest debtor until every balance is
zero).

## Reflect (10 min)

1. `settlesThreeWayCaseCorrectly` checks the settlement's NET EFFECT matches the original
   balances, rather than asserting one exact list of transfers. Why is that the right thing to
   assert here, given the greedy algorithm's tie-breaking is a genuine implementation choice?
2. `SettlementCalculator.settle` rejects balances that do not sum to zero. What real bug in an
   upstream `Ledger` would this validation actually catch, before it reached a customer as a
   wrong payment amount?
3. The greedy algorithm is not guaranteed to produce the MINIMUM possible number of transfers for
   every input. Sketch - you do not need to build it - what information an optimal algorithm
   would need that the greedy one discards.

**Interview angle:** "optimal account balancing" (its actual algorithmic name) is a well-known
problem, and interviewers watching you solve it care most about the INVARIANT you protect at
every step: total money in equals total money out, everywhere, always. State that invariant
explicitly before you start coding the greedy loop - it is both the correctness argument and the
reason the "sum to zero" validation exists at all.

## Stretch

Extend `Ledger` to support MULTIPLE independent groups (a "weekend trip" group and a "roommates"
group sharing some but not all members), each with its own settlement. What changes about how
balances are keyed, and does `SettlementCalculator` need to know groups exist at all?

## Checkpoint

```powershell
.\day.cmd 37
```
