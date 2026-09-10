# Day 50 - Phase review: measure the shard

**Phase 5 - Databases** | 45 minutes

## Concept (10 min)

Start by rereading your `NOTES.md` from days 41-49. Five minutes. Half of this phase was numbers
you produced - query plans, pool ceilings, query counts - and those decay fastest.

Today's subject is the discipline that makes all of it real: **measure, then state the trade-off
with a number attached.**

Yesterday you knew salting fixes a hot key. Knowing is cheap. Today you run a realistic skewed
workload and compute the **hot-shard load factor** - how much more traffic the busiest shard
takes than the average. Then you salt, run the same workload at the same scale, and watch the
factor fall.

That is what turns "salting helps" into "salting took our hot shard from 8.4x average to 1.3x at
this skew". The second sentence is an engineering result. The first is folklore, and folklore is
what gets quoted confidently and applied in the wrong place.

The third test is the one to take seriously: **salting changes distribution, never the true
total.** An optimisation that changes your answers is not an optimisation, it is a bug with good
performance. Every time you shard, cache, denormalize or batch in the rest of this course, the
same question applies - did the numbers stay correct?

## Build (25 min)

In `src/main/java/sd/p05/day50/ShardingBenchmark.java`, run the skewed workload, time it, and
compute the hot-shard load factor. The tests assert:

1. An unsalted skewed workload produces a clearly measurable load factor.
2. Salting flattens it, at the same scale.
3. Salting changes distribution but not the true total.

Then spend whatever is left writing. Create `docs/notes/day-50-storage-tradeoffs.md` with one
row per decision from this phase:

| Decision | Buys you | Costs you | When it is wrong |
|---|---|---|---|
| Index | read speed | write cost, disk | write-heavy ingest |
| Covering index | no heap fetch | more write cost, more disk | rarely-run query |
| Denormalized view | one round trip | two writes, consistency debt | write-heavy, or truth matters more than latency |
| LSM engine | sequential writes | read and space amplification | read-heavy point lookups |
| WAL + fsync | durability | flush latency per write | data you can regenerate |
| Hash sharding | even distribution | no range queries, hot keys | range-scan workloads |
| Salting | spreads a hot key | fan-in on read | uniform traffic |

Fill in your own numbers from this phase's measurements. This table is the single most useful
artefact you will produce in Phase 5 - it is what you will actually reach for in a design
discussion.

## Reflect (10 min)

1. What load factor did you measure before and after salting? At what skew does salting stop
   being worth the read-side complexity?
2. Look back at Day 42's plans and Day 45's pool ceiling. Which of this phase's numbers would you
   now quote from memory in a design review?
3. Which decision in the table above would you defend hardest, and which do you think you would
   most often get wrong?

## Phase 5 retrospective

Write in `NOTES.md`:

- The one thing about databases you believed on Day 40 and no longer believe
- The measurement that most surprised you
- The one thing still fuzzy - carry it into Phase 6 explicitly

Then tick days 41-50 in `PROGRESS.md` and commit.

## Looking ahead

Phase 6 is caching, and it opens with a claim you are now equipped to test: a cache is the
cheapest scalability win available, and the richest source of production incidents. Day 56 is
consistent hashing - the answer to the resharding problem you met yesterday.

## Checkpoint

```powershell
.\day.cmd 50
```
