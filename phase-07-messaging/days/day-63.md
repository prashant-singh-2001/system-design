# Day 63 - Consumer groups and delivery semantics

**Phase 7 - Messaging** | 45 minutes

## Concept (10 min)

**Consumer groups.** Every partition is assigned to exactly one consumer within a group. So a
group scales up to - and no further than - the partition count. Add a sixth consumer to a
five-partition topic and it sits idle. That is why partition count is a capacity decision you
make early and change painfully.

Different groups are independent and each sees everything, which is Day 61's log property doing
the work.

**Delivery semantics.** Here is the whole thing, and it is smaller than people expect. Both
methods you are about to write poll, process and commit. The only difference is the **order of
the last two steps**:

- **Process, then commit.** Crash in between and the offset never advanced, so the next consumer
  reprocesses. **At-least-once**: duplicates, never loss.
- **Commit, then process.** Crash in between and the offset moved past a record nobody handled.
  **At-most-once**: loss, never duplicates.

There is no third option. This is the practical meaning of "exactly-once does not exist": you
pick which failure you prefer, then make the consequence harmless. Almost every real system picks
at-least-once plus idempotent processing (Day 74), because **a duplicate you can absorb beats a
message you cannot recover**.

One trap worth knowing: Kafka's default `enable.auto.commit=true` commits on a timer, in the
background, whether or not your processing succeeded. That is neither of the two semantics above -
it is at-most-once wearing a disguise, and it is why so many teams discover they were quietly
losing messages.

**The trade-off:** at-least-once forces idempotency work onto every consumer. That is real
engineering cost, paid so that failures are recoverable rather than silent.

## Build (25 min)

In `src/main/java/sd/p07/day63/ConsumerLab.java`:

1. `atLeastOnce` - poll, process, then `commitSync()`. When `crashBeforeCommit`, return without
   committing.
2. `atMostOnce` - poll, `commitSync()`, then process. When `crashAfterCommit`, return immediately
   with nothing processed.
3. `awaitAssignment` - subscribe, then poll until `assignment()` is non-empty. The loop is
   necessary and instructive: subscribing only expresses interest, and until the coordinator has
   run a rebalance the consumer owns nothing.

## Reflect (10 min)

1. In the at-least-once test, every record was handled twice. If processing charges a card, what
   have you just done? What has to change?
2. In the at-most-once test, four records vanished and nothing raised an error. Which failure
   would you rather debug at 3am, and why?
3. Your topic has 12 partitions and you run 20 consumers. What are the other 8 doing, and what
   would you change?

**Interview angle:** "at-least-once delivery with idempotent consumers" should be your default,
and the sentence that earns it is "exactly-once delivery is not achievable across a network, so
we make processing idempotent instead".

## Stretch

Start a consumer, let it get an assignment, then start a second one in the same group and watch
the rebalance move partitions. Then make one consumer slow enough to miss its heartbeat and watch
it get evicted mid-work - that is the failure mode behind most "why did we reprocess?" incidents.

## Checkpoint

```powershell
.\day.cmd 63
```
