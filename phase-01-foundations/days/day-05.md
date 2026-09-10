# Day 5 - Four ways to count, and what each costs

**Phase 1 - Foundations** | 45 minutes

## Concept (10 min)

Yesterday you made a counter correct. Today you make it correct four different ways and find out
that "correct" is where the interesting question starts, not where it ends.

- **`synchronized`** - the JVM starts with a cheap biased/thin lock. Under real contention it
  *inflates*: threads park in the kernel, and a contended acquire costs microseconds rather than
  nanoseconds. Simple, correct, and the slowest under load.
- **`ReentrantLock`** - same cost profile, more capability: `tryLock`, timeouts,
  interruptibility, fairness. A *fair* lock hands ownership to the longest waiter, eliminating
  starvation and costing roughly an order of magnitude in throughput. Rarely worth it.
- **`AtomicLong`** - lock-free. A compare-and-swap instruction retries in user space until it
  wins. No kernel involvement, dramatically faster at low contention. But every thread is
  CAS-ing the same cache line, so at high contention the retries and the cache-line ping-pong
  between cores dominate.
- **`LongAdder`** - stops threads fighting over one cell by keeping an array of them and letting
  threads hash to different cells. `sum()` adds them up on read.

That last move is the one to remember. **When a single hot resource is contended, shard it.**
You will meet this exact idea again as sharded databases in Phase 5 and partitioned Kafka topics
in Phase 7. It is the same idea at three different scales.

**The trade-off:** `LongAdder` trades read cost and atomic-snapshot semantics for write
throughput. Perfect for a metrics counter written constantly and scraped once a minute. Wrong
if you need to read-and-act on the value, because `sum()` is not a consistent snapshot.

## Build (25 min)

`SynchronizedCounter` is given as the reference. Implement the other three in
`src/main/java/sd/p01/day05/`. Each is only a few lines - the value today is in the benchmark
output and in being able to explain the ordering you see.

## Reflect (10 min)

1. Write down the ops/sec you measured for all four. Is the ordering what you expected?
2. Re-run with 2 threads instead of 8. Does the ordering change? Why would low contention favour
   a different winner?
3. You need a request counter for a metrics endpoint scraped every 60 seconds. Which do you
   choose, and what exactly are you giving up?

**Interview angle:** "we will use an AtomicLong" is fine. "AtomicLong up to moderate contention;
past that every thread is CAS-ing one cache line, so I would use LongAdder and accept that
reads are no longer a consistent snapshot" is a different level of answer.

## Stretch

Add a fifth implementation that pads the counter to a full cache line (or uses `@Contended`) and
see whether false sharing was costing you anything. Then explain what false sharing is in one
sentence.

## Checkpoint

```powershell
.\day.cmd 5
```
