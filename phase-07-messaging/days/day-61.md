# Day 61 - A topic is a log, not a queue

**Phase 7 - Messaging** | 45 minutes

Start Docker Desktop. The first run pulls the Kafka image and takes a couple of minutes.

## Concept (10 min)

Almost everything surprising about Kafka follows from one sentence:

> A topic is a partitioned, append-only **log**, not a queue.

Work through the consequences:

- **Reading does not remove anything.** Messages stay until a retention policy drops them, so two
  independent consumer groups read the same topic without interfering. One topic can feed
  analytics, billing and search simultaneously. A queue cannot do that - it hands each message to
  exactly one reader.
- **A consumer's position is just an offset** - a number it remembers. Rewinding is setting that
  number backwards, which is why replay is trivial here and effectively impossible with a
  traditional queue.
- **A topic is split into partitions**, and a partition is the unit of both parallelism and
  ordering. Ordering holds within a partition and nowhere else - that is Day 64, and it is the
  most consequential fact in the phase.
- **The key chooses the partition.** Equal keys always land together. Every ordering guarantee you
  will rely on comes from this.

And writes are sequential appends to a file, which is why Kafka is fast on cheap disks - the same
Day 1 fact that produced the LSM tree on Day 47.

**The trade-off:** keeping everything costs disk, and retention becomes a real decision rather
than an afterthought. In exchange you get replay, multiple independent readers, and the ability
to add a consumer next year that reads history written today.

## Build (25 min)

In `src/main/java/sd/p07/day61/TopicExplorer.java`:

1. `publish` - send each keyed message and return where the broker actually put it.
   `producer.send(record)` returns a `Future<RecordMetadata>`; `get()` blocks until the broker
   acknowledges and hands back the partition and offset.
2. `drain` - subscribe and poll until you have the expected count or time out.

Two things worth internalising while you write `drain`:

- **The first poll usually returns nothing.** It triggers the group join and partition assignment.
  Polling once and concluding the topic is empty is the single most common Kafka mistake.
- **`poll` is not a peek.** It is the heartbeat that keeps this consumer in the group. A consumer
  that stops polling is presumed dead and its partitions are reassigned - which is why slow
  processing inside the poll loop causes rebalances.

## Reflect (10 min)

1. The last test read the same six messages twice, from two groups. Name a system design that
   becomes possible because of this and is awkward with a queue.
2. Retention is a business decision. What breaks if you set it to one hour? To one year?
3. Partition count caps consumer parallelism and is painful to change later. How would you pick
   it for a topic expecting 10,000 messages/second?

**Interview angle:** "Kafka is a log, so consumers track their own offsets and multiple teams can
read the same stream independently" is the sentence that shows you understand the model rather
than treating it as a faster RabbitMQ.

## Stretch

Use `consumer.seek(...)` to rewind to offset 0 and reprocess. You have just done a replay - which
is the mechanism behind rebuilding a projection from scratch (Day 68).

## Checkpoint

```powershell
.\day.cmd 61
```
