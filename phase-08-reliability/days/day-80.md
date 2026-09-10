# Day 80 - Phase review: harden it

**Phase 8 - Reliability** | 45 minutes

## Concept (10 min)

Reread your `NOTES.md` from days 71-79 first. Five minutes.

Today you compose everything, and the composition is the lesson. Each mechanism alone is
incomplete:

- Retries without a breaker hammer a dead service.
- A breaker without a bulkhead lets a merely **slow** dependency exhaust your threads.
- Neither helps if retrying corrupts data - that is what idempotency is for.
- And none of it means anything without an SLO to measure against.

Reliability is a stack, and **the order matters**:

```
bulkhead  -> is there capacity for this dependency at all?    (isolate)
  breaker -> do we believe it is up?                          (fail fast)
    retry -> transient? back off, jittered, on budget          (recover)
      call
```

Outermost is the cheapest rejection. A bulkhead rejection costs a semaphore check; a breaker
rejection costs a state read; a retry costs real time. Reject as early and cheaply as you can. The
ordering also means a retry storm cannot bypass the bulkhead - which it could if you nested them
the other way round.

And the outermost behaviour of all: **return empty rather than throwing.** A bounded, fast,
predictable failure the caller can plan for - a cached value, a default, a partial page - is the
entire point of the stack. An exception propagating to the user is the stack having failed.

The closing test is the phase's result: a dependency failing one call in ten, and the service still
meets a 99% SLO. Nothing in it is clever on its own. Composed, in the right order, ordinary
mechanisms turn a dependency's bad day into a number you can defend.

## Build (25 min)

Implement `HardenedClient.call` in `src/main/java/sd/p08/day80/`. The one subtlety: on
`CircuitBreakerOpenException`, **stop immediately - do not retry**. The breaker has already decided
the dependency is down; retrying against an open breaker is pointless work and defeats the purpose
of having one.

Then spend what is left writing `docs/notes/day-80-reliability-tradeoffs.md`:

| Decision | Buys you | Costs you | When it is wrong |
|---|---|---|---|
| CP under partition | never wrong | minority side unavailable | carts, feeds, counters |
| AP under partition | always up | divergence to reconcile | balances, inventory, locks |
| Retry + jitter | absorbs transient failure | amplifies overload without a budget | non-idempotent writes |
| Retry budget | cannot amplify an outage | some recoverable requests fail | when retries are already rare |
| Circuit breaker | fails fast, stops propagation | fails requests that might work | very bursty dependencies |
| Bulkhead | one dependency cannot starve others | fixed capacity per dependency | a single dependency |
| Idempotency keys | retries are safe | a store, a TTL, key hygiene | genuinely read-only operations |
| Quorum W+R>N | reads see writes | latency, and still not linearizable | when eventual is enough |
| Consensus | provably one leader | majority round trip, stops without a majority | the data path |
| Fencing tokens | zombie writes rejected | the resource must cooperate | when no shared resource exists |
| Error budgets | ends the ship-vs-stabilise argument | requires an agreed SLO | no measurement in place |

## Reflect (10 min)

1. Record the availability your hardened client achieved against a 10% failure rate. Which single
   mechanism contributed most, and how would you check that claim?
2. Reverse the nesting - retry outside the bulkhead. What breaks?
3. Which of the eleven decisions above would you defend hardest, and which do you think you would
   most often get wrong?

## Phase 8 retrospective

Write in `NOTES.md`:

- The one thing about reliability you believed on Day 70 and no longer believe
- The failure mode you would now actively look for in a design review
- The one thing still fuzzy - carry it into Phase 9 explicitly

Then tick days 71-80 in `PROGRESS.md` and commit.

## Looking ahead

Phase 9 is the synthesis: nine written designs and a three-day capstone. By now none of the
components are mysterious - you have built a cache, a rate limiter, a sharded store, a consumer
group, a leader election and this defence stack. High-level design stops being vocabulary recall
and becomes what it should be: choosing between things you have used, and defending the choice.

## Checkpoint

```powershell
.\day.cmd 80
```
