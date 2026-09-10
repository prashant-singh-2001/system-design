# Day 60 - Phase review: prove the win

**Phase 6 - Caching** | 45 minutes

## Concept (10 min)

Reread your `NOTES.md` from days 51-59 first. Five minutes.

Today's subject is the discipline that makes a performance claim mean something: **percentiles,
not averages, over a representative workload.**

Take 99 requests at 1 ms and one at 1,000 ms. The mean is about 11 ms - a number that sounds fine
and describes nobody's experience. Averages hide exactly the tail you are paid to care about.

Worse, on a page that makes 100 backend calls, a p99 backend latency is hit by roughly **63%** of
page loads (`1 - 0.99^100`). At fan-out the tail is not an edge case, it is the common case. That
one calculation is the best argument for percentiles there is, and it is worth being able to
produce on demand.

The second half of the discipline is the **workload**. A uniform random key distribution over a
large key space makes any cache look useless. Real traffic is Zipfian - a small set of keys takes
most of the requests - which is why a cache holding 5% of the key space still catches most of the
traffic. Choosing a representative workload is most of the work in benchmarking, and the most
common place a benchmark quietly lies.

Then the result you are about to measure, which is the phase's real conclusion:

> **A cache improves the median far more than the tail.**

Hits collapse p50. Misses still pay full price, so p99 barely moves. That means **a cache is not
a fix for a slow dependency - it is a fix for a busy one.** If you need the tail as well, you
need the source itself to be fast, or Day 54's early recompute so that a user never waits on a
refill. Being able to say that distinguishes someone who has measured a cache from someone who
has added one.

## Build (25 min)

In `src/main/java/sd/p06/day60/`:

1. `LatencyRecorder` - nearest-rank percentiles (`ceil(p/100 x n) - 1`, clamped), mean, count.
   An empty recorder returns zero rather than throwing; a metrics call must never be the thing
   that breaks a request path.
2. `CacheBenchmark` - run the same Zipfian workload uncached and cached, and report both.

Watch the summary lines print, then spend what is left of the session writing
`docs/notes/day-60-caching-tradeoffs.md`:

| Decision | Buys you | Costs you | When it is wrong |
|---|---|---|---|
| Cache-aside | simple, resilient to cache loss | every miss pays full latency | write-heavy, read-rarely |
| Write-through | never stale, read-your-writes | every write pays twice | writes far outnumber reads |
| Write-behind | fastest writes, coalescing | unflushed writes are lost | anything you cannot lose |
| LRU | simple, matches most workloads | a scan flushes it | scan-heavy access |
| Single-flight | one load per hot key | a stuck load blocks waiters | uncontended keys |
| Jittered TTL | no synchronised expiry | slightly variable freshness | never, really |
| Local cache + pub/sub | nanosecond reads | stale window, missed messages | strong consistency needed |
| Consistent hashing | ~1/N keys move on resize | ring state to agree on | a fixed, never-resized fleet |
| CDN `max-age` | removes load entirely | irreversible until it expires | content that must change now |

Fill in your own numbers. This table is what you will actually reach for in a design discussion.

## Reflect (10 min)

1. Record your p50 and p99, cached and uncached, plus the hit ratio. Explain the p99 in one
   sentence.
2. Your dependency takes 500 ms and is called 10 times per second. Does caching help? Now make it
   10,000 times per second. What changed, and why is that the whole distinction?
3. Which decision in the table would you defend hardest, and which do you think you would most
   often get wrong?

## Phase 6 retrospective

Write in `NOTES.md`:

- The one thing about caching you believed on Day 50 and no longer believe
- The failure mode you would now actively look for in a code review
- The one thing still fuzzy - carry it into Phase 7 explicitly

Then tick days 51-60 in `PROGRESS.md` and commit.

## Looking ahead

Phase 7 is messaging. It opens with an availability argument you can now check yourself: chain
five synchronous services and your uptime is the product of theirs. Day 65's outbox pattern is
the answer to a problem you already met on Day 46 - two writes, two systems, no atomicity.

## Checkpoint

```powershell
.\day.cmd 60
```
