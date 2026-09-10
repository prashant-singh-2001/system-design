# Day 73 - Circuit breakers and bulkheads

**Phase 8 - Reliability** | 45 minutes

## Concept (10 min)

Retries assume the problem is transient. A **circuit breaker** handles the case where it is not.

When a dependency is genuinely down, retrying is actively harmful: you burn your own threads
waiting on timeouts and add load to something already struggling. So the breaker trips, and calls
fail immediately without touching it.

The insight is counter-intuitive and worth keeping: **failing fast is a feature.** An immediate
error lets you serve a degraded response - a cached value, a default, a partial page - in
microseconds. A timeout gives the user thirty seconds of nothing and then the same error, having
occupied a thread the whole time.

That thread occupation is the real danger. If every call to a dead dependency parks a thread for
its timeout, your pool fills with doomed calls and requests that had nothing to do with that
dependency start failing too. One sick service takes down a healthy one. A breaker stops the
propagation at the boundary.

The **HALF_OPEN** state matters more than it looks: without it a breaker either stays open forever
or slams the full load back onto a service that has just come up, and knocks it straight over
again.

A **bulkhead** - named after a ship's compartments - solves the adjacent problem. A breaker reacts
to *failure*; a bulkhead prevents one dependency from consuming resources the others need, whether
or not it is failing. A merely **slow** dependency never trips a breaker and can still occupy
every thread you have.

Concretely: recommendations and payments share a thread pool. Recommendations degrade to 10-second
responses. Within a minute every thread is waiting on recommendations and payments - perfectly
healthy - starts failing. You have coupled your most important path to your least important one.

Separate permit budgets make that impossible, and they mean you **chose in advance which feature
degrades under pressure**. That is what "graceful degradation" means in code.

**The trade-off:** a breaker fails requests that might have succeeded. Too low a threshold and a
blip causes a self-inflicted outage; too high and it never protects you. And a breaker per instance
learns independently, so a fleet of fifty takes fifty times as many failures to react.

## Build (25 min)

In `src/main/java/sd/p08/day73/`:

1. `CircuitBreaker` - the CLOSED / OPEN / HALF_OPEN state machine, driven by an injected `Clock`
   so the timing is testable without sleeping.
2. `Bulkhead` - `tryAcquire` a permit without blocking, run, and **release in a finally block**.
   A leaked permit is a slow-motion outage that only appears under load.

## Reflect (10 min)

1. The fail-fast test made 50 requests and zero calls. What did each of those 50 requests cost,
   and what would they have cost without the breaker?
2. Your breaker trips at 5 consecutive failures. What blip would that misread? What would you
   change, and what would you give up?
3. In the isolation test, payments kept working while recommendations was saturated. Which of your
   own dependencies deserve separate bulkheads, and how would you size them?

**Interview angle:** "a circuit breaker so a dead dependency fails fast, and a bulkhead per
dependency so a slow one cannot exhaust the pool" names both failure modes. Most answers name only
the breaker, which leaves the slow case wide open.

## Stretch

Swap your hand-rolled breaker for Resilience4j and compare. You will find it uses a sliding
*window* of failures rather than consecutive ones - work out which is better and when.

## Checkpoint

```powershell
.\day.cmd 73
```
