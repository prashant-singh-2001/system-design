# Day 62 - Producer durability and batching

**Phase 7 - Messaging** | 45 minutes

## Concept (10 min)

The `acks` setting is the clearest cost/benefit dial in the whole broker:

- **`acks=0`** - fire and forget. The producer does not wait at all, and **does not even learn
  where the record went** - the returned metadata carries no offset. A network blip loses data
  silently. Only for telemetry you can afford to lose.
- **`acks=1`** - the partition leader has written it. Fast, and you get an offset. But if the
  leader dies before a follower replicates, that record is gone. This was the old default and the
  source of a great many "we lost messages" incidents.
- **`acks=all`** - every in-sync replica has it. Survives a leader failure, at the cost of a round
  trip to the slowest in-sync replica.

Then **idempotence**, which fixes a subtler problem. A producer that sends, times out, and retries
may have had the first attempt succeed - so the retry creates a duplicate. An idempotent producer
tags each batch with a producer id and sequence number and the broker discards repeats.

Notice that Kafka **refuses** to enable idempotence with `acks=1`. Deduplication is meaningless if
the record can vanish with a dead leader, so rather than trusting you to get the combination
right, it fails at construction. Enforcing coherent configuration instead of documenting it is a
design instinct worth stealing.

Finally **batching**. `linger.ms` makes the producer wait briefly to fill a batch - deliberately
adding latency to gain throughput. Day 1's "batching beats chatter", as a config knob.

But batching only works if you let it. **Calling `get()` after every send serialises everything
into one round trip per record**, and no `linger.ms` setting on earth can rescue it. That is the
single most common reason a Kafka producer is slow, and it is invisible in code review unless you
know to look.

**The trade-off:** durability costs latency, batching costs latency, and both buy throughput.
There is no setting that is simply "best" - only one that matches what the data is worth.

## Build (25 min)

In `src/main/java/sd/p07/day62/ProducerLab.java`:

1. `sendAndDescribe` - send one record, block for the ack, and report whether the broker actually
   gave you an offset. That single boolean is the difference `acks=0` makes.
2. `sendBatch` - fire all the sends, then `flush()` once, and return the elapsed time.

Do not swallow send failures. An `InterruptedException` restores the interrupt flag; an
`ExecutionException` surfaces as `IllegalStateException`. A swallowed send failure is how data
disappears.

## Reflect (10 min)

1. With `acks=0` the producer never learned the offset. What else does it not learn, and what
   would that mean during a broker restart?
2. Your data is payment events. Which `acks` setting, and what is your justification if someone
   objects to the latency?
3. `linger.ms=50` adds up to 50 ms of latency per record. When is that free, and when is it
   unacceptable?

**Interview angle:** "`acks=all` with an idempotent producer for anything that matters, and
`linger.ms` tuned to the latency budget" is a complete producer configuration story - and being
able to say *why Kafka refuses `acks=1` with idempotence* is the detail that shows real use.

## Stretch

Set `max.in.flight.requests.per.connection` above 5 with idempotence enabled and watch Kafka
refuse again. Work out why in-flight ordering matters for deduplication - the answer is the same
sequence-number mechanism.

## Checkpoint

```powershell
.\day.cmd 62
```
