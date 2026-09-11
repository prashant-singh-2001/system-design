# Notes

Your running journal. Ten minutes a day, and it is the part that makes the rest stick - the Build
block teaches your hands, this teaches your memory.

Append a new entry each day. Do not go back and tidy old ones: seeing that you were confused about
something on Day 12 and confident about it on Day 47 is the point.

At every phase review (days 10, 20, 30 ...) reread this file's entries for that phase before
starting. Five minutes. It is the single highest-leverage habit in the programme, because system
design knowledge decays fast without retrieval practice.

Longer write-ups - the trade-off tables and estimation exercises - go in `docs/notes/` instead.
Design documents go in `phase-09-hld-capstone/designs/`.

---

## Template

Copy this for each day.

```
## Day N - <title>

**Date:** | **Time spent:**

**What I built:**

**The three questions:**
1.
2.
3.

**The trade-off in one line:** <every concept buys something at a cost - name both>

**Interview angle:** <the one sentence that shows I understand this>

**Still fuzzy:** <write it down; fuzzy things compound if left alone>
```

---

## Day 1 - The memory hierarchy, measured

**Date:** 9th of September, 2026 | **Time spent:** 35 Mins

**What I built:** I built a simple app to show difference between latencies for Sequential reads vs Random Access reads. (Seq are approx 71x better) 

**The three questions:**
1. The ratio I measured was around (50 -70) : 1, while cheatsheet predicts approx 100:1. Problem due to system being used by other resources.
2. Array of int is faster to sum as the values are stored sequentially so CPU can pre-fetch those. Approximately (50-100) : 1. 
3. The ratio dropped from 70 to 7. Small dataset (1-2 MB) can be accessed via random access as they are still in L2  cache, while larger dataset (>20MB) will create miss which will add RAM seek.

**The trade-off in one line:** Locality is free performance, but it constrains your data layout. Arrays are fast and rigid; linked structures are flexible and cache-hostile.

**Interview angle:**  When someone proposes a design that reads scattered rows in a loop, the sentence you want is: "that is a random-access pattern — each row is likely a separate page fetch, so we are paying about N disk seeks rather than one sequential scan." Same principle, one level down the hierarch

**Still fuzzy:**

---

## Day 2 - Back-of-the-envelope estimation

**Date:** 10th September, 2026 | **Time spent:** 32 Minutes

**What I built:** A simple estimator class to find the estimated readQPS, writeQPS and storage related queries.

**The three questions:**
1. Cache aggressively (50:1 reads). add read reps and denormlize for quick fetch
2. No it does not fit on one machine, this tells me that the storage and compute systems should be distributed. You must partition horizontally (sharding). Split users/data across dozens of machines by range or hash, each shard holding ~10–20 TB, replicated 3x. A single machine is no longer viable; you're forced into distributed storage.
3. At 200k–231k reads/sec, the decision to build a read-heavy architecture doesn't change. The error only matters when estimates land near decision boundaries (e.g., if true QPS was 9,000 and rounding puts you at 10,000, pushing you across a "single database" vs. "sharded database" threshold). Here, 200k is so far above those boundaries that 15% is noise.

**The trade-off in one line:** Rounding aggressively costs you accuracy and buys you speed and confidence.

**Interview angle:** The 15% rounding is acceptable here because it doesn't change the architectural decision — we're so clearly read-heavy at 50:1 that ±15% doesn't flip us to a write-optimized design. I'd call it out anyway, so the estimate stays transparent.

**Still fuzzy:** 

---

## Day 3 - Little's Law and the queueing knee

**Date:** 11th September, 2026 | **Time spent:** 28 Min

**What I built:** A simple class to validate throughput and concurrency of a system

**The three questions:**
1.  Since 200req/s and p50 of 0.1, in flight is 20 req (needed threads) + 70% headroom == 30 threads with 50$ as extra.
2. Maximum throughput drops 67% (from 1,000 to 333 req/s). The pool is saturated with requests taking 3x longer. Requests queue up, latencies explode, and you can't process traffic fast enough. This is why you need headroom — a fixed pool can't adapt when dependencies degrade.
3.  Scale at 60–70% because:
    - Autoscaling takes time (instance provisioning, warmup, DNS propagation)
    - You need buffer time before new instances come online
    - At 80%+, you're already in the vertical part of the curve—by the time new capacity appears, users have experienced a p99 latency explosion for minutes

**The trade-off in one line:** Costs vs Throughput choke, needs to be decided not just flinged

**Interview angle:** I'd set the threshold at 70% utilization. Below that, the response curve is still reasonable. Above that, each percent increase hits disproportionately harder. Since autoscaling is reactive and takes time, I want to trigger it while I still have headroom, not when I'm already at the cliff.

**Still fuzzy:** 