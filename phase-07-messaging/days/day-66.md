# Day 66 - Backpressure

**Phase 7 - Messaging** | 45 minutes

No Docker today - this is pure Java.

## Concept (10 min)

An **unbounded** queue looks like the safe choice and is the dangerous one. Produce faster than
you consume and it grows without limit. The failure mode is not a clean error: it is memory
climbing for twenty minutes, then GC thrashing, then an `OutOfMemoryError` that takes down the
whole process - including all the healthy work it was doing.

You have converted a throughput problem into an availability incident, and you did it by
accepting work you could not do. **Accepting work you cannot do is a lie you tell yourself in
units of megabytes.**

A **bounded** queue forces the decision to be made explicitly, early, while you still have
options. There are exactly three options and no fourth:

- **BLOCK** - make the producer wait. Backpressure propagates upstream: the producer slows to the
  consumer's rate, nothing is lost, latency rises. The right default when the producer is
  something you control.
- **DROP_NEWEST** - refuse the newest item. Latency stays bounded and you shed load deliberately.
  The right choice when the producer is the outside world and you would rather serve some users
  well than all users badly.
- **DROP_OLDEST** - refuse the oldest to make room. For data where fresh beats complete - live
  telemetry, price ticks, sensor readings - where an old value has no value.

And the rule that outranks all three:

> **A queue absorbs bursts. It cannot fix a rate mismatch.**

If production exceeds consumption on average, no buffer size saves you. It only changes how long
you wait before finding out. Little's Law (Day 3) tells you what depth to pick for bursts: at
1,000 items/second and 50 ms of processing you need 50 in flight, so a few hundred absorbs spikes
without hiding a sustained problem.

## Build (25 min)

In `src/main/java/sd/p07/day66/BoundedPipeline.java`:

1. `unboundedGrowth` - offer items to a `LinkedBlockingQueue` with no consumer, and return the
   depth. Nothing fails; nothing warns. That is the point.
2. `run` - start a consumer thread, then offer items under the given policy, tracking the deepest
   the queue ever got. `put` for BLOCK, `offer` for DROP_NEWEST, and `poll`-then-`offer` for
   DROP_OLDEST.

## Reflect (10 min)

1. Your service reads from Kafka and writes to a slow API. Which policy, and what does the choice
   say about what you value?
2. The last test showed a 50x bigger buffer still dropping. Explain in one sentence why.
3. BLOCK propagates backpressure upstream. What happens when "upstream" is an HTTP request from a
   user? Is blocking still right?

**Interview angle:** "we use bounded queues so overload becomes a deliberate load-shedding
decision rather than an OutOfMemoryError" is a strong operational statement, and naming *which*
overflow policy and why makes it a design one.

## Stretch

Add a metric for queue depth and think about where you would alert. Sustained depth near capacity
is the signal that you have a rate mismatch rather than a burst - and catching that distinction
early is most of capacity planning.

## Checkpoint

```powershell
.\day.cmd 66
```
