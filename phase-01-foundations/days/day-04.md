# Day 4 - The JVM memory model

**Phase 1 - Foundations** | 45 minutes

## Concept (10 min)

Two things go wrong when threads share data, and they are not the same thing.

**Atomicity.** `count++` is three operations: read, add, write. Two threads can read the same
value, both add one, and both write back the same result. One increment vanishes. Nothing
throws. The number is just quietly wrong, and it will be wrong by a different amount every run.

**Visibility.** Even a single write may never become visible to another thread. Each core has
its own caches and store buffers, and the compiler is free to reorder and hoist reads. Without a
*happens-before* edge, the JVM has no obligation to show one thread's write to another - ever.
A spin loop reading a plain `boolean` flag can be compiled into an infinite loop, because
nothing inside the loop modifies the field.

The Java Memory Model defines exactly which operations create happens-before edges: releasing a
monitor, writing a `volatile`, starting a thread, `Thread.join`, and the concurrent library's
own guarantees.

The distinction to hold on to: **`volatile` gives you visibility, not atomicity.** It fixes the
flag. It does not fix `count++`.

**The trade-off:** every one of these edges is a memory barrier, which constrains the CPU and
the compiler. Correctness under concurrency is bought with performance. That is exactly why
`LongAdder` exists (tomorrow) and why lock-free structures are hard.

## Build (25 min)

Two small tasks in `src/main/java/sd/p01/day04/`:

1. **`SafeCounter`** - make increments atomic. `synchronized`, `AtomicLong` or a
   `ReentrantLock` all work. Pick one and be ready to say what it costs.
2. **`StopSignal`** - make the flag visible across threads. This is a one-keyword fix.

Do **not** fix `UnsafeCounter`. Its test asserts only that it never exceeds the true count,
because asserting that it *does* lose updates would be flaky - and that flakiness is itself the
lesson. A race that passes today fails in production under load.

## Reflect (10 min)

1. Your run lost some number of increments. Run it again. Different number? What does a bug that
   changes magnitude every run do to your ability to debug it from logs?
2. Why is `volatile long count; count++;` still broken? Be precise.
3. The visibility test spins in an empty loop. Add a `System.out.println` inside and it will
   probably start passing even without the fix. Why? What does that tell you about "it works on
   my machine" for concurrency bugs?

**Interview angle:** "we will just make it volatile" is a very common wrong answer. Being able
to say "volatile gives visibility but not atomicity, and increment is read-modify-write, so we
need a CAS or a lock" is a strong signal.

## Stretch

Write the classic double-checked locking singleton without `volatile`, and work out precisely
which reordering breaks it. This is the canonical example of why the memory model needed
specifying at all.

## Checkpoint

```powershell
.\day.cmd 4
```
