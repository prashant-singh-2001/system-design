# Day 24 - Decorator + Proxy: middleware, caching, lazy loading

**Phase 3 - Patterns** | 45 minutes

## Concept (10 min)

**Decorator**: wrap the same interface you are given, delegate to it, and add exactly one
concern. Stack decorators and you get middleware - which is precisely what an HTTP framework's
filter chain, a servlet's filter stack, and today's `SlowService` pipeline all are underneath
their different names.

**Proxy** is structurally the SAME shape - implement an interface, hold a delegate, forward
calls - with a different INTENT: not adding behaviour, but controlling access to or the
lifecycle of the real object. `CachingDecorator` is honestly both at once: from the caller's
side it is a proxy standing in for the real service, transparently answering some requests
itself; the mechanism that makes it work is a decorator wrapping the same interface. Today's
`LazyServiceProxy` is Proxy for its OTHER classic reason: deferring an expensive construction
until the first real use.

**Order matters, and it is not arbitrary.** Stack `Timing(Caching(Retry(Flaky)))` and think
through why that specific order, not some other one: `Retry` has to sit directly against the
unreliable remote service, because retrying a cache is pointless. `Caching` has to sit around
`Retry`, so that once a value is known-good, later requests skip both the retry logic and the
remote call entirely. `Timing` has to sit OUTSIDE everything, so every call gets measured -
cache hit or miss, first attempt or third retry - because a timing layer that only sees some
calls is blind to exactly the ones you most need visibility into.

## Build (25 min)

In `src/main/java/sd/p03/day24/`:

1. **`TimingDecorator`** - measure elapsed time around the delegate call in a `try/finally`,
   reporting it to the given callback whether the call succeeds or throws.
2. **`CachingDecorator`** - serve a hit without touching the delegate; on a miss, call through
   and cache only a SUCCESSFUL result.
3. **`RetryDecorator`** - retry up to `maxAttempts` total attempts, rethrowing the last failure
   if every attempt fails. Reject `maxAttempts < 1`.
4. **`LazyServiceProxy`** - build the real `SlowService` via the given factory on first use only,
   then reuse it for every call after.

## Reflect (10 min)

1. `fullStackComposesCorrectly` asserts the SECOND call - a pure cache hit - is still measured by
   `TimingDecorator`. Explain concretely what you would be blind to in production if timing sat
   INSIDE caching instead of outside it.
2. Swap the stack to `Retry(Caching(Flaky))` in your head. What breaks, specifically, the first
   time the flaky service fails on a key that was never cached?
3. `LazyServiceProxy` is explicitly not thread-safe today. Describe the race: two threads both
   call `fetch` for the first time, concurrently. What could go wrong, and how does it connect to
   Day 28's correct, thread-safe singleton?

**Interview angle:** naming "Decorator" or "Proxy" is not the signal - the signal is ordering a
stack of cross-cutting concerns correctly and being able to defend the order, the way you just
defended timing-outermost above. Interviewers ask "where would you put retry vs. caching" far
more often than "what is the Decorator pattern."

## Stretch

Add a `CircuitBreakerDecorator` that stops calling the delegate entirely - failing fast with a
dedicated exception - after N consecutive failures, resetting after a cooldown. You are building
Day 73's circuit breaker in miniature; notice which existing decorator it most resembles in
shape.

## Checkpoint

```powershell
.\day.cmd 24
```
