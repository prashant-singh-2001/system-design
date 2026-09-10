# Day 54 - Cache stampede

**Phase 6 - Caching** | 45 minutes

## Concept (10 min)

Read `NaiveCache`. Look for the bug. There isn't one - every individual request behaves
perfectly correctly.

Now put a thousand concurrent requests through it on a hot key that has just expired. Every one
of them misses at the same instant. Every one calls the database. One expiry becomes a thousand
simultaneous queries, at your busiest moment, against the database the cache existed to protect.

That is a **cache stampede** - also called a thundering herd, or dog-piling. What makes it so
nasty is that the failure is *emergent*: it exists only under concurrency, so it never appears in
testing, and the code review that would have caught it has nothing to point at.

There are actually two distinct stampedes, and they need different fixes:

**One hot key, many concurrent readers.** Fix with **single-flight**: one loader per key,
everyone else waits for its result. `ConcurrentHashMap.computeIfAbsent` already does this - it
holds the key's bin while the mapping function runs, so concurrent callers for the same key
block, and different keys still proceed in parallel. The whole fix is one line, which is worth
sitting with: a subtle bug with a trivial remedy is exactly the kind that keeps getting shipped.

**Many keys expiring together.** A deploy populates ten thousand keys within one second, all with
a 60-minute TTL, so an hour later they all expire together. Single-flight does nothing here -
these are ten thousand *different* keys. Fix with **jittered TTL**: draw each expiry from a range
around the base, so deadlines scatter. Same idea as jittered retry backoff on Day 72: whenever
many independent actors share a deadline, add noise.

The third technique, **probabilistic early recompute**, avoids the expiry moment entirely. As an
entry ages, each reader independently rolls a die that gets progressively more likely to say
"refresh this now". Somebody almost certainly refreshes before it expires, so the moment when
nobody holds a value never arrives. You do slightly more work than strictly necessary and buy a
p99 that never contains a load.

**The trade-off:** single-flight makes concurrent readers wait on one loader, so a genuinely
stuck load turns a stampede into a pile-up - pair it with a timeout in production.

## Build (25 min)

In `src/main/java/sd/p06/day54/`:

1. `SingleFlightCache.get` - one line, once you see it.
2. `JitteredTtl.jitter` - uniform over `[base x (1 - jitter), base x (1 + jitter)]`.
3. `EarlyRecompute.shouldRefreshEarly` - nothing before `beta` of the entry's life, then a linear
   ramp to certainty; always true at or past the TTL.

## Reflect (10 min)

1. The naive cache made many source calls; single-flight made exactly one. Did any individual
   reader's latency get worse? Why not?
2. Why does single-flight do nothing for the mass-expiry stampede? Be precise about what it
   deduplicates.
3. Early recompute does redundant work by design. At what hit rate does that stop being worth it?

**Interview angle:** "we would use single-flight so one loader repopulates a hot key, plus
jittered TTLs so a deploy does not create a synchronised expiry" names both stampedes. Naming
only the first is the common half-answer.

## Stretch

Add a timeout to the single-flight load, so a stuck loader fails fast for its waiters instead of
holding them. That converts a pile-up into a handful of errors - which is Phase 8's whole subject.

## Checkpoint

```powershell
.\day.cmd 54
```
