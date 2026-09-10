# Day 72 - Timeouts, retries and jitter

**Phase 8 - Reliability** | 45 minutes

## Concept (10 min)

Retries are the most dangerous reliability feature you can add, because they are the only one that
makes an overload **worse**.

A dependency slows down. Every client retries. Load triples. The dependency slows further. You
have built a positive feedback loop into your own architecture, and a brief degradation becomes a
total outage. That is a **retry storm**.

Three strategies, each fixing the previous one's flaw:

- **Fixed delay** - keeps hammering at a constant rate. Does nothing to relieve the thing that is
  struggling.
- **Exponential** - `base x 2^(attempt-1)`, capped. Backs off fast and gives the dependency room.
  But every client that failed at the same moment retries at the same moment, repeatedly -
  synchronised waves of load.
- **Full jitter** - a uniform draw from `[0, exponential(attempt)]`. Keeps the backoff and destroys
  the synchronisation. AWS's published analysis found it beats both no-jitter and half-and-half on
  total work and completion time. **Randomness here is the mechanism, not a tweak.**

You met this on Day 54 as jittered cache TTLs and will meet it again tomorrow in Raft's election
timeouts. Whenever many independent actors share a deadline, add noise.

Then the piece backoff cannot give you. Backoff spaces retries out; it does not bound how many
there are. If a dependency is failing for everyone, backoff still lets everyone retry - politely -
and aggregate load can still be several times normal.

A **retry budget** caps retries as a fraction of *successful* traffic. Allow 10%: at 1,000
successes you may spend 100 retries. When the dependency is healthy that is plenty; when it is
failing there are no successes, the budget empties, and retries stop almost entirely.

That inversion is the whole idea, and it is the opposite of naive retry logic: **the budget is
most generous when you least need it and most restrictive when retrying would hurt.**

## Build (25 min)

In `src/main/java/sd/p08/day72/`:

1. `BackoffStrategy` - `fixed`, `exponential` (capped), `fullJitter`. Attempts are 1-based.
2. `RetryBudget` - `recordSuccess` earns tokens up to a cap; `tryConsume` spends one or refuses.
3. `RetryStormSimulator.peakConcurrentRetries` - bucket retries into 100 ms slots and return the
   busiest. That peak, not the total, is what decides whether the dependency recovers.

## Reflect (10 min)

1. Write down the two peak numbers you measured. Explain the difference in one sentence.
2. Retries need timeouts to be bounded at all. What is a retry without a timeout? (It is a hang.)
3. Your service retries three times with full jitter and a 10% budget. A dependency goes down
   completely. Trace what your traffic to it looks like over the next minute.

**Interview angle:** "exponential backoff with full jitter, bounded attempts, and a retry budget
so we cannot amplify an outage" is a complete retry policy. Most answers stop at "we retry with
backoff" and miss both the jitter and the budget.

## Stretch

Add a **deadline** that propagates: a request arrives with 500 ms of budget, and each retry
subtracts its elapsed time. Once it is gone, stop - even mid-policy. That is how gRPC deadlines
work, and it is what stops a retry chain outliving the client that wanted the answer.

## Checkpoint

```powershell
.\day.cmd 72
```
