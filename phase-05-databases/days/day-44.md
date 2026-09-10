# Day 44 - Locks, deadlocks and MVCC

**Phase 5 - Databases** | 45 minutes

## Concept (10 min)

A deadlock is not exotic. It needs only two transactions taking the same two locks in opposite
orders:

```
  T1: lock A ... lock B
  T2: lock B ... lock A
```

Both hold what the other needs, and neither can proceed. Postgres detects the cycle after a
second and kills one transaction with a `deadlock detected` error. Your application sees a
failed transaction with no obvious cause, at a rate that scales with traffic, which is why
deadlocks are so often first diagnosed as "flaky".

The fix is beautifully simple and worth remembering forever: **always take locks in a consistent
global order.** If both transactions lock the lower id first, one of them simply waits, finishes,
and lets the other through. No cycle is possible, because a cycle requires disagreement about
order. One `Math.min` / `Math.max` in the right place removes an entire class of production
incident.

The other half of today is **MVCC** - multi-version concurrency control - and its single most
important consequence:

> Readers do not block writers, and writers do not block readers.

A plain `SELECT` reads a snapshot and never waits, even while another transaction holds an
uncommitted write lock on the very row it is reading. That is why Postgres sustains heavy read
traffic during writes, and it is why "the read query is waiting on a lock" is usually a wrong
diagnosis.

The exception is a **locking read**: `SELECT ... FOR UPDATE` genuinely does block, because you
have explicitly asked to serialize against other writers. Knowing precisely which reads block and
which do not is the practical payoff of understanding MVCC at all.

**The trade-off:** MVCC keeps old row versions around, so they must be cleaned up. That is what
`VACUUM` is for, and it is why a long-running transaction is expensive - it pins old versions and
causes table bloat. Every design has a cost somewhere; MVCC's is deferred to a background job.

## Build (25 min)

In `src/main/java/sd/p05/day44/DeadlockLab.java`:

1. `lockAndRead` - `SELECT balance ... FOR UPDATE`, return the balance.
2. `transferUnordered` - lock `fromId`, run a test hook (to force the interleaving), lock `toId`,
   apply the changes, commit. This one deadlocks under crossing transfers, by design.
3. `transferOrdered` - lock `min(fromId, toId)` first, then `max`, then apply and commit.

The tests then run crossing transfers both ways: the unordered version genuinely deadlocks, and
the ordered version never does.

## Reflect (10 min)

1. Both transfer methods do identical work and touch identical rows. Explain in one sentence why
   only one deadlocks.
2. A plain read did not block on an uncommitted write, but `FOR UPDATE` did. When do you actually
   want `FOR UPDATE`, and what does it cost?
3. Your deadlock rate rises with traffic. Besides lock ordering, name two other ways to reduce it.

**Interview angle:** "we order lock acquisition by primary key, so crossing transfers queue
instead of deadlocking" is a specific, credible answer. Compare it with "we add a retry", which
treats the symptom and leaves the rate rising with load.

## Stretch

Reproduce the deadlock, then read `pg_stat_activity` and the server log during it. Learning to
recognise a real deadlock message is worth more than any amount of theory when you are on call.

## Checkpoint

```powershell
.\day.cmd 44
```
