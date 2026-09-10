# Day 75 - Replication and quorums

**Phase 8 - Reliability** | 45 minutes

## Concept (10 min)

Three replication topologies, and what each costs:

- **Leader-follower** (Postgres, MySQL, Kafka partitions) - all writes go to one node. Ordering is
  easy, and the leader is both a bottleneck and a failover event waiting to happen.
- **Multi-leader** (cross-region active-active) - writes accepted in several places. Local write
  latency, and you must resolve conflicts.
- **Leaderless** (Dynamo, Cassandra, Riak) - every replica takes reads and writes. No failover at
  all, because there is nothing to fail over. You pay with per-operation quorum arithmetic.

The quorum rule is one line, and the reasoning behind it is worth more than the formula:

> **W + R > N** guarantees that every read set overlaps every write set.

If the sets must share at least one replica, and that replica has the latest write, the read can
see it. Nothing subtler is happening.

The knobs are yours per operation, which is the real appeal:

- `W=N, R=1` - fast reads; one dead replica blocks every write
- `W=1, R=N` - fast writes; one dead replica blocks every read
- `W=R=(N+1)/2` - the balanced default. With N=3, W=R=2: one replica down and everything still works

Two things today will show you that are easy to miss:

**A failed write can still have landed.** The store reports failure because it did not reach a
quorum - and one replica has the value anyway. Once the others recover, that value can win a read.
So a client seeing a write error cannot conclude the write did not happen, which is exactly why
Day 74's idempotency is not optional.

**Read repair** is how a leaderless system heals: every read is an opportunity to push the winning
value to stale replicas. Convergence is a side effect of normal traffic rather than a background job.

**The caveat worth carrying into an interview:** quorum overlap alone does **not** give you
linearizability. Concurrent writes can still be applied in different orders at different replicas.
"We use W+R>N so it is strongly consistent" is a common and confident mistake.

## Build (25 min)

In `src/main/java/sd/p08/day75/`:

1. `QuorumConfig` - `guaranteesOverlap`, and the two fault-tolerance figures.
2. `QuorumStore` - `write` (all reachable, need W), `read` (gather R, newest version wins),
   `readRepair` (push the winner to stale replicas).

## Reflect (10 min)

1. With N=3, W=3, R=1: what is your write availability, and when is that configuration correct?
2. Explain the partial-write test to a colleague who says "the write failed, so nothing happened".
3. Why does W+R>N not give linearizability? Construct the interleaving that breaks it.

**Interview angle:** "N=3 with W=R=2 tolerates one replica down for both reads and writes, and we
use read repair to converge - though quorum alone is not linearizable" is a complete, honest answer.

## Stretch

Add **hinted handoff**: when a replica is unreachable, another holds its write and delivers it on
recovery. That is how Cassandra keeps write availability high without losing data - and it is what
turns the partial write you saw today from an accident into a mechanism.

## Checkpoint

```powershell
.\day.cmd 75
```
