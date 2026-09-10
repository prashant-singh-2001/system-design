# Day 29 - Producer-consumer, bounded buffers, poison pills

**Phase 3 - Patterns** | 45 minutes

## Concept (10 min)

**Producer-consumer**: one or more producers add work to a shared queue; one or more consumers
take it off and process it, at their own pace. `WorkerPool` puts several consumer THREADS on the
same `BlockingQueue`, which is the shape underneath every thread-pool executor you have
configured without necessarily building one from scratch.

**Bounded buffer**: a queue with a fixed capacity turns a fast producer and a slow consumer into
automatic BACKPRESSURE instead of unbounded memory growth. `submit()`'s call to `queue.put()`
BLOCKS once the queue is full - the producer is forced to slow down to the consumer's pace,
which is exactly the property you want. An unbounded queue here would just move the outage from
"caller waits" to "caller keeps enqueueing until the process runs out of heap," which is worse.

**Poison pill**: the classic way to tell N consumer threads to stop, cleanly, without a shared
"please stop" flag every consumer has to remember to poll. Put exactly one pill PER WORKER onto
the queue; because pills go to the BACK of the queue behind whatever real work was already
there, every already-submitted job gets drained and processed BEFORE any worker sees its pill
and exits. `shutdown()` then joins every worker thread, so it does not return until every thread
has genuinely stopped - no thread quietly left running after the method that was supposed to
stop it returns.

The other property worth building deliberately: **one bad job must not kill its worker.** A
`try/catch` around each job's processing, recording a failure as data rather than letting the
exception propagate and terminate the worker thread, is the difference between "one malformed
message degrades throughput by 1/N" and "one malformed message slowly kills the whole pool, job
by job, worker by worker."

## Build (25 min)

In `src/main/java/sd/p03/day29/`, implement `WorkerPool`:

- constructor: start `workerCount` threads, each looping `queue.take()` -&gt; if a
  `PoisonPill`, exit the loop; otherwise process the `Job`, catching any `RuntimeException` and
  recording a `Result.failure` instead of dying.
- `submit(job)`: reject with `IllegalStateException` after shutdown; otherwise `queue.put(job)`.
- `shutdown()`: one poison pill per worker, then join every worker thread.
- `results()` / `liveWorkerCount()`: the two windows a test (or an operator) has into what the
  pool is actually doing.

## Reflect (10 min)

1. `submitBlocksWhenQueueIsFull` proves backpressure works by timing a blocked submission.
   Describe, in plain terms, what an UNBOUNDED queue would have let happen instead under the
   same load, and why "it still eventually processes everything" is not actually a defence.
2. Exactly one poison pill per worker, not one pill total. What specifically goes wrong -
   describe it precisely - if `shutdown()` enqueues only a single pill regardless of
   `workerCount`?
3. `aFailingJobDoesNotKillItsWorker` submits a job AFTER the one that fails and confirms it still
   gets processed. What would the test have looked like, and what would it have proven, if
   `processor.apply(job)` were called with NO try/catch around it at all?

**Interview angle:** "we'd use a thread pool" undersells what you actually understand. The
strong version explains the three properties by name and WHY each matters - bounded buffer for
backpressure, poison pills (or an equivalent shutdown signal) for clean termination, per-job
exception isolation so one bad message cannot cascade into a dead worker - because those three
are exactly what "just use `ExecutorService`" is quietly doing for you underneath.

## Stretch

Add a `submitBatch(List<Job> jobs)` that enqueues all of them and blocks until every one has a
recorded `Result` - a synchronous "process this batch and wait" API layered on top of the
asynchronous pool underneath. What is the simplest correct way to know when a whole batch is
done, given that `results()` accumulates from every submission ever made, not just this batch's?

## Checkpoint

```powershell
.\day.cmd 29
```
