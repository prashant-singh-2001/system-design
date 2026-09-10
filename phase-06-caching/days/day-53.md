# Day 53 - Eviction, TTL and hit ratio

**Phase 6 - Caching** | 45 minutes

## Concept (10 min)

Three mechanisms that people conflate, and they are genuinely different:

- **Capacity** bounds **memory**. When the cache is full, something must go, and the eviction
  policy chooses what.
- **TTL** bounds **staleness**. An entry past its deadline is gone regardless of free space,
  because the data may no longer be true.
- **Stats** tell you whether any of it is working.

**LRU** bets that what was used recently will be used again. True for most workloads, which is
why it is the default nearly everywhere. Its weakness: one large scan - a backup job, an
analytics query walking every row - touches everything once and flushes your genuinely hot data
out. That is **cache pollution**, and it is why production caches use LRU-K or segmented LRU.

**LFU** bets that what has been popular will stay popular. It resists the scan, because one touch
does not outrank a thousand. Its own weakness is the mirror image: last week's viral item keeps
its high count and squats forever while a genuinely rising key cannot displace it. Real LFU
implementations add decay for exactly this reason.

Then the number that decides everything: **hit ratio**. Below roughly 80% you are paying for two
systems and a consistency risk in exchange for a modest win. The only way to know is to measure -
which is why an uninstrumented cache is essentially unmanageable, and why every serious cache
library exposes these counters.

**The trade-off:** a bigger cache raises the hit ratio with diminishing returns and rising cost.
A longer TTL raises the hit ratio and raises staleness. Both dials trade something real, and
neither has a correct setting you can look up - only one you can measure.

## Build (25 min)

In `src/main/java/sd/p06/day53/`:

1. `LruEvictionPolicy` - a `LinkedHashMap` with `accessOrder = true` maintains the order for you;
   the eldest key is the least recently used.
2. `LfuEvictionPolicy` - counts per key. Break ties by smallest key, so behaviour is
   deterministic and therefore testable.
3. `InstrumentedCache` - capacity, TTL against an injected `Clock`, and the four counters.

Two details the tests check: **overwriting an existing key must not evict anything**, and an
expired entry must be removed from memory rather than merely reported absent.

## Reflect (10 min)

1. A nightly export scans every row. What does that do to an LRU cache, and what would you change?
2. Your hit ratio is 60%. Name three different actions that could raise it, and what each costs.
3. TTL and capacity both remove entries. Which one protects correctness, and which protects the
   machine?

**Interview angle:** "we would size the cache for the hot 20% and set the TTL from how stale the
data may safely be - then watch the hit ratio and adjust" ties Day 10's estimation to an
operational habit, which is exactly the join interviewers listen for.

## Stretch

Implement segmented LRU: a small probation segment for new entries and a protected segment for
entries hit twice. Re-run the scan scenario and watch the pollution disappear.

## Checkpoint

```powershell
.\day.cmd 53
```
