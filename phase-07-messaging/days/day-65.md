# Day 65 - The outbox pattern

**Phase 7 - Messaging** | 45 minutes

Start Docker Desktop - this day uses a real Postgres *and* a real Kafka.

## Concept (10 min)

You met this problem on Day 46 without a name for it. An order is placed and two things must
happen: a row goes into the database, and an event goes onto Kafka. Two different systems, no
transaction spanning both, and either can fail independently.

- **Write the database, then publish.** If the publish fails, the order exists and nobody
  downstream ever hears about it. No email, no shipment, no analytics - **and no error anywhere**,
  because the order itself succeeded. Silent, and therefore the worst kind.
- **Publish, then write the database.** If the write fails, you have announced an order that does
  not exist. Downstream services act on a phantom.
- **Two-phase commit.** Slow, operationally miserable, and Kafka does not support it anyway.

The **outbox pattern** dissolves the problem rather than solving it. Write the order row and an
outbox row **in the same local database transaction**. One transaction, one system - atomic, with
no distributed anything. A separate **relay** then reads unpublished outbox rows, publishes them,
and marks them published.

The relay may crash after publishing and before marking, so a row can be published twice. That is
at-least-once, chosen deliberately, for Day 63's reason: **duplicates you can absorb beat messages
you cannot recover.** Consumers handle it with idempotency.

**The trade-off:** events are published slightly late (relay latency), and you now operate a relay
and a table that needs pruning. In exchange you get a guarantee otherwise unavailable - the event
exists if and only if the order does.

## Build (25 min)

In `src/main/java/sd/p07/day65/`:

1. `OrderService.placeOrder` - `setAutoCommit(false)`, insert the order, insert the outbox row
   with `published = false`, `commit()`. Roll back and rethrow on failure, and **restore
   autoCommit in a finally block** - leaving a pooled connection in manual mode is a nasty
   surprise for whoever borrows it next.
2. `OrderService.placeOrderWithoutOutbox` - the broken version, for contrast.
3. `OutboxRelay.fetchUnpublished` - unpublished rows, **ordered by id ascending**. The id sequence
   is the order events happened in; publishing out of order hands consumers a lie.
4. `OutboxRelay.relayOnce` - publish keyed by `aggregateId`, block for the ack, **then** mark
   published. Never the reverse.

## Reflect (10 min)

1. Write out what happens if the relay crashes between publishing and marking. Now write out what
   happens if you mark first. Which failure would you rather have?
2. The outbox table grows forever. What is your pruning strategy, and what must you be careful
   about?
3. Events are published a moment after the order commits. Name a requirement that this latency
   would violate, and what you would do instead.

**Interview angle:** the moment a design needs "save to the database and publish an event",
saying "I would use a transactional outbox so the two cannot diverge, and accept at-least-once
delivery with idempotent consumers" resolves the whole question in one sentence.

## Stretch

Replace the polling relay with change data capture - Postgres logical replication, or Debezium.
Same guarantee, no polling, and the relay stops being your code. Then work out what you gave up
in exchange (hint: a database-specific dependency, and a new operational surface).

## Checkpoint

```powershell
.\day.cmd 65
```
