# Day 28 - Singleton done right; object pools; flyweight

**Phase 3 - Patterns** | 45 minutes

## Concept (10 min)

Three patterns today, unified by one theme: controlling how many instances of something exist,
for three different reasons.

**Singleton**, done right, means lazy AND thread-safe without paying for a lock on every access.
The classic wrong answer is double-checked locking with a `volatile` field - it works, but it is
easy to get subtly wrong, and there is a better idiom: the INITIALIZATION-ON-DEMAND HOLDER. A
private static nested class is not loaded until something first calls `instance()`, and the JVM
already guarantees a class initialises at most once, with a happens-before edge for anything
that triggers it. You get laziness and thread safety from a mechanism the JVM already had to
provide for `static` initializers in general - no synchronization code of your own required.

**Object Pool**: some objects are genuinely expensive to create - a database connection, in
spirit if not in today's exercise - and a bounded pool of reusable ones, gated by a
`Semaphore`, turns "create one per request" into "reuse one of at most N", with the semaphore
providing exactly the backpressure a fixed resource needs: `borrow()` BLOCKS once the pool is
exhausted, rather than letting unlimited callers pile up expensive resources at once.

**Flyweight**: when the SAME immutable value - a country code, a currency, a permission level -
gets referenced from millions of records, share one instance instead of constructing a new one
per reference. `CountryFactory` interns `Country` objects exactly the way `Integer.valueOf` and
`String.intern()` already do for you in the standard library, for exactly the same reason.

## Build (25 min)

In `src/main/java/sd/p03/day28/`:

1. **`AppConfig`** - the holder-idiom singleton.
2. **`ObjectPool<T>`** - `borrow()` acquires a semaphore permit (blocking if none free) then
   reuses an idle instance or creates one; `release(item)` returns the item to the idle queue
   THEN releases the permit - in that order, or a concurrent `borrow()` can slip in and create a
   redundant new instance instead of reusing the one you were about to hand back.
3. **`CountryFactory`** - cache and return the SAME `Country` instance per ISO code; throw
   `IllegalArgumentException` for an unrecognised one.

## Reflect (10 min)

1. Explain, precisely, why the holder idiom needs no `synchronized` and no `volatile` anywhere -
   what mechanism is actually doing the thread-safety work, and where does the JVM already
   guarantee it for reasons that have nothing to do with the Singleton pattern?
2. `release` orders "return to queue" before "release permit" on purpose. Describe the race that
   is possible if you reverse those two lines under concurrent load.
3. Flyweight only pays off when a value is both IMMUTABLE and referenced a huge number of times.
   Name one value type in a system you have worked on that would have been a good flyweight
   candidate, and one that would NOT have been (say why not).

**Interview angle:** "Singleton is an anti-pattern" is a half-true reflex some interviewers will
push back on - the more defensible position is that GLOBAL MUTABLE state accessed via `new
Foo()` scattered everywhere is the actual anti-pattern, and a properly-scoped singleton for a
genuinely single, expensive, stateless-or-carefully-synchronized resource (a connection pool, a
thread pool, a config snapshot) is a completely reasonable, narrow use of the pattern.

## Stretch

Add `ObjectPool.tryBorrow(Duration timeout)`, returning `Optional.empty()` if no permit becomes
available within the timeout instead of blocking forever - `Semaphore.tryAcquire(timeout, unit)`
does the heavy lifting. Now your pool has the same bounded-wait shape a real HikariCP connection
pool acquisition uses, which you will meet for real on Day 45.

## Checkpoint

```powershell
.\day.cmd 28
```
