# Day 70 - Phase review: the whole pipeline

**Phase 7 - Messaging** | 45 minutes

Start Docker Desktop - Postgres and Kafka.

## Concept (10 min)

Reread your `NOTES.md` from days 61-69 first. Five minutes.

Today you assemble everything into one pipeline:

```
API -> [orders + outbox, one transaction] -> relay -> Kafka -> consumer -> projection
```

Every hop uses something you built:

- **Day 65** - the order and its event are written atomically, so the event exists if and only if
  the order does.
- **Day 62** - the relay publishes with `acks=all` and an idempotent producer, so a retry cannot
  duplicate a batch at the broker.
- **Day 64** - events are keyed by *customer*, so one customer's events stay ordered while
  different customers proceed in parallel.
- **Day 63** - the consumer processes before committing: at-least-once, so nothing is lost.
- **Day 74, borrowed early** - because delivery is at-least-once, the projection tracks which
  event ids it has applied and ignores repeats.

That last piece is the phase's conclusion, and it deserves to be a slogan:

> **Exactly-once *delivery* does not exist. Exactly-once *processing* does - through idempotency.**

Every reliable event-driven system is at-least-once delivery plus idempotent consumers. Anyone
selling you the first is selling you the second with worse marketing.

Watch what the duplicate-suppression test proves: without idempotency, one redelivery - caused by
a rebalance, a replay, a redeploy, nobody doing anything wrong - silently doubles a customer's
revenue. That is the failure the whole phase has been building toward.

## Build (25 min)

In `src/main/java/sd/p07/day70/OrderPipeline.java`:

1. `placeOrder` - one transaction, two inserts, restore autoCommit in a finally block.
2. `relay` - publish unpublished rows oldest-first, **keyed by customer id**, ack, then mark.
3. `consumeIntoProjection` - read the `event-id` header, skip ids already processed (counting
   them), otherwise fold `customerId:totalCents` into the revenue map. Commit after processing.

Then spend what is left writing `docs/notes/day-70-messaging-tradeoffs.md`:

| Decision | Buys you | Costs you | When it is wrong |
|---|---|---|---|
| `acks=all` | survives leader loss | round trip to slowest replica | telemetry you can lose |
| Idempotent producer | no duplicate batches on retry | forces `acks=all` | never, really |
| Keying by entity | ordering per entity | hot partitions if too coarse | when order does not matter |
| At-least-once | nothing is lost | consumers must be idempotent | when duplicates are unacceptable and loss is not |
| Outbox | event iff the order exists | relay latency, a table to prune | when a small divergence is tolerable |
| Bounded queue | overload is a decision | you must choose what to drop | when the producer is already rate-limited |
| DLQ | pipeline keeps moving | needs alerting and replay | when partial processing is worse than stopping |
| Event sourcing | audit trail, new read models | schema forever, snapshots, deletion | simple CRUD |
| Event-time windows | correct under delay | latency vs completeness trade | when approximate is fine |

## Reflect (10 min)

1. The duplicate test suppressed one event. Describe, precisely, the incident you avoided.
2. Trace a single order through all five hops and name the guarantee each one provides.
3. Which of the nine decisions above would you defend hardest, and which do you think you would
   most often get wrong?

## Phase 7 retrospective

Write in `NOTES.md`:

- The one thing about messaging you believed on Day 60 and no longer believe
- The failure mode you would now actively look for in a design review
- The one thing still fuzzy - carry it into Phase 8 explicitly

Then tick days 61-70 in `PROGRESS.md` and commit.

## Looking ahead

Phase 8 removes the last comfortable assumption: that things mostly work. Networks partition,
clocks drift, nodes die mid-write, and naive retries turn one slow dependency into a thundering
herd. Day 74 formalises the idempotency you just borrowed, and Day 76 has you implement Raft
leader election.

## Checkpoint

```powershell
.\day.cmd 70
```
