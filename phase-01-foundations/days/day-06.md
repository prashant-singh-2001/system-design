# Day 6 - Virtual threads

**Phase 1 - Foundations** | 45 minutes

## Concept (10 min)

A platform thread is an OS thread: about 1 MB of reserved stack, scheduled by the kernel. You can
have thousands, not millions. So for blocking work you pool them - and the pool size becomes a
hard throughput ceiling, exactly the ceiling Little's Law described on Day 3.

That constraint shaped a decade of backend design. It is why async/reactive frameworks exist: if
threads are expensive, do not block one, hand back a callback instead. The cost was that your
code stopped looking like the problem it was solving. Stack traces became useless, debuggers
became useless, and `try/catch` stopped meaning anything.

A virtual thread is a continuation on the heap - a few hundred bytes, scheduled by the JVM. When
it blocks it *unmounts* from its carrier thread, and the carrier goes and runs something else.
Blocking stops being expensive. You write straightforward sequential code and get the concurrency
of an async framework.

Today you measure the difference: 5,000 tasks each blocking for 20 ms. On a pool of 50 platform
threads that is 100 rounds - about 2 seconds. On virtual threads they all block at once, so total
time approaches a single task duration.

**The trade-off:** virtual threads are for blocking, I/O-bound work. For CPU-bound work they buy
you nothing - you still only have as many cores as you have, and a pool sized to core count is
still right. Two sharp edges remain: `synchronized` blocks can *pin* a virtual thread to its
carrier (use `ReentrantLock` in hot paths), and cheap threads make it easy to overwhelm a
downstream service that was protected by your pool size acting as an accidental rate limiter.

## Build (25 min)

Implement `runOnVirtualThreads` in `src/main/java/sd/p01/day06/ThreadingLab.java` using
`Executors.newVirtualThreadPerTaskExecutor()`. Reuse the given `blockFor` and `await` helpers so
the comparison against the platform version is fair.

There is no pool size to choose. That is the point.

## Reflect (10 min)

1. What speedup did you measure, and what does the ratio correspond to arithmetically?
2. Change the task from `sleep` to a busy CPU loop of the same duration. Predict what happens
   before you run it, then run it. Were you right?
3. Your service calls a downstream API that handles 500 req/s. Previously your 50-thread pool
   capped you below that by accident. What do you now need to add explicitly?

**Interview angle:** "with virtual threads, thread-per-request scales to hundreds of thousands of
concurrent connections, so the async complexity is no longer the price of concurrency - but
blocking is still not free downstream, so I would put an explicit semaphore or rate limiter
where the thread pool used to be." That last clause is what separates a read-about-it answer
from a used-it answer.

## Stretch

Try 1,000,000 virtual threads. Then try 10,000 platform threads and watch it fall over. Note the
actual failure mode - it is worth having seen.

## Checkpoint

```powershell
.\day.cmd 6
```
