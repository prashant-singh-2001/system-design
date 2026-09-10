# Day 64 - Ordering and the partition key

**Phase 7 - Messaging** | 45 minutes

## Concept (10 min)

The guarantee, stated exactly:

> Kafka guarantees ordering **within a partition**. Across partitions there is no ordering at all.

Because the key chooses the partition, **the key is your ordering unit**. Key by `orderId` and
every event for that order is strictly ordered - created before paid before shipped - while
different orders proceed in parallel. Key by nothing and records round-robin across partitions,
so a consumer may legitimately see "shipped" before "created", and there is no bug to find: you
never asked for ordering.

That makes partition key choice a **correctness** decision, not a performance one. It is the
single most consequential line in most event-driven designs, and it is usually written without
much thought.

Then the constraint that ties the phase together. Ordering requires related records to go to one
partition, and one partition is handled by one consumer. So:

> **Perfect ordering means no parallelism.**

Global ordering means one partition, which means one consumer, which means your throughput
ceiling is a single machine. You almost never want that. What you want is ordering *per entity*,
which is exactly what keying gives you - and it is the same trade as Day 49's sharding, because
it is literally the same mechanism.

**The trade-off, and the trap:** a key that is too coarse (say, a country code) creates a hot
partition, which is Day 49's hot key wearing a different hat. A key that is too fine gives up the
ordering you needed. Choosing it well requires knowing what actually has to be ordered - which is
a domain question, not a Kafka question.

## Build (25 min)

In `src/main/java/sd/p07/day64/OrderingLab.java`:

1. `publishKeyed` - publish an ordered sequence under one key, blocking on each send so the order
   is unambiguous, and return the partition they all landed on.
2. `publishUnkeyed` - the two-argument `ProducerRecord`, no key, and return the partitions used.
3. `drainByPartition` - group delivered values by partition, preserving each partition's own
   order. That grouping is what makes the guarantee visible: within each list the order is exactly
   what was produced; the interleaving *between* lists is arbitrary.

(Blocking per send is deliberately wrong for throughput, as Day 62 showed. It is right here
because we are demonstrating ordering, not speed.)

## Reflect (10 min)

1. You are designing events for a food delivery app. What do you key by, and what does that
   guarantee - and fail to guarantee?
2. A country-code key sends 60% of traffic to one partition. Name the Day 49 problem this is, and
   two ways to fix it.
3. The last test achieved total ordering with one partition. Compute the throughput ceiling if a
   single consumer handles 2,000 messages/second and you need 50,000.

**Interview angle:** "we key by user id, so a user's events are strictly ordered while different
users process in parallel" is precise and shows you know ordering is bought with parallelism
rather than being free.

## Stretch

Write a custom `Partitioner` that routes a known hot key across several partitions while keeping
everything else keyed normally. You have just built Day 49's salting, at the broker's edge - and
you can now say exactly what ordering you gave up to get it.

## Checkpoint

```powershell
.\day.cmd 64
```
