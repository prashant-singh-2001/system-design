# Day 71 - CAP, PACELC and consistency models

**Phase 8 - Reliability** | 45 minutes

No Docker this phase - it is all plain Java.

## Concept (10 min)

CAP is the most misquoted theorem in the field. It does **not** say "pick two of three". It says
something much narrower:

> **During a network partition**, you must choose between availability and consistency.

When there is no partition you get both - which is almost all of the time. Describing a database
as "AP" or "CP" as a general property is a misuse of it.

The choice, once a partition happens, is real and stark:

- **CP** - the minority side refuses writes rather than risk diverging. Correct, and partly
  unavailable. Right when a wrong answer costs more than no answer: balances, inventory,
  distributed locks.
- **AP** - both sides accept and diverge, and you reconcile later. Up, and temporarily wrong.
  Right when no answer costs more than a slightly wrong one: carts, feeds, view counts.

Today you will also watch the reconciliation. Last-write-wins silently discards one side's change,
with no error and no record - which is rarely an acceptable *product* decision even when it is an
easy engineering one. That is why real AP systems reach for vector clocks or CRDTs.

**PACELC** is the more useful framing:

> If **P**artition, choose **A** or **C**; **E**lse, choose **L**atency or **C**onsistency.

The "else" half is where you live 99.9% of the time, and CAP ignores it entirely. Even on a
perfectly healthy network, waiting for replicas costs latency. The trade never goes away; it just
stops being about availability.

Finally, the consistency spectrum. The skill is picking the **weakest model that satisfies the
requirement**. Notice how few systems genuinely need linearizability: most "we need strong
consistency" requirements turn out to be read-your-writes, which is enormously cheaper - a routing
decision rather than a consensus round trip.

## Build (25 min)

In `src/main/java/sd/p08/day71/`:

1. `PartitionedStore` - `write` (CP refuses on the minority side; AP accepts everywhere), `read`
   (always local), and `heal` (last-write-wins by version).
2. `ConsistencyChooser.choose` - apply the five rules in order; the strongest applicable wins.
3. `ConsistencyChooser.pacelc` - classify a system as `PC/EC`, `PA/EL`, `PC/EL` or `PA/EC`.

## Reflect (10 min)

1. The healing test discarded one side's write. Write the incident report a customer would file.
2. Take a system you work on. What is its PACELC classification, and was that ever a decision?
3. Which of your "strongly consistent" requirements are actually read-your-writes? What would
   change if you said so?

**Interview angle:** "CAP only binds during a partition; the rest of the time the real trade is
PACELC's latency-versus-consistency" corrects the most common misuse and shows you have read past
the slogan.

## Stretch

Replace last-write-wins with a vector clock, so concurrent writes are detected as a *conflict*
rather than silently ordered. Then decide what your application would do with one - which is the
part LWW lets you avoid thinking about.

## Checkpoint

```powershell
.\day.cmd 71
```
