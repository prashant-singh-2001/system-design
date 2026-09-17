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

**Interview angle:** When someone proposes a design that reads scattered rows in a loop, the sentence you want is: "that is a random-access pattern — each row is likely a separate page fetch, so we are paying about N disk seeks rather than one sequential scan." Same principle, one level down the hierarchy.

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
1. Since 200 req/s and p50 of 0.1s, in-flight is 20 req (needed threads) + 70% headroom == 30 threads with 50% as extra.
2. Maximum throughput drops 67% (from 1,000 to 333 req/s). The pool is saturated with requests taking 3x longer. Requests queue up, latencies explode, and you can't process traffic fast enough. This is why you need headroom — a fixed pool can't adapt when dependencies degrade.
3. Scale at 60-70% because:
   - Autoscaling takes time (instance provisioning, warmup, DNS propagation)
   - You need buffer time before new instances come online
   - At 80%+, you're already in the vertical part of the curve — by the time new capacity appears, users have experienced a p99 latency explosion for minutes

**The trade-off in one line:** Headroom costs money and buys latency stability — it needs to be a deliberate decision, not something left to chance.

**Interview angle:** I'd set the threshold at 70% utilization. Below that, the response curve is still reasonable. Above that, each percent increase hits disproportionately harder. Since autoscaling is reactive and takes time, I want to trigger it while I still have headroom, not when I'm already at the cliff.

**Still fuzzy:** 

---

## Day 4 - The JVM memory model

**Date:** 15th September, 2026 | **Time spent:** 15 Mins

**What I built:** Simple atomic counter and volatile flags

**The three questions:**

1. Yes, the lost count changes every run — perhaps 127 lost on run 1, 342 on run 2, 89 on run 3. That makes the bug nearly impossible to diagnose from production logs because:
   - The symptom is non-deterministic and non-reproducible
   - You see the wrong final count, but you can't pinpoint which increments were lost or when
   - Thread interleavings differ on each run — same code, same input, different failure
   - Logs show "expected 1,000,000, got 999,127" but you can't trace it to a specific thread or line
   - Running with debuggers/logging often masks the bug (instrumentation adds pauses that serialize access)

   Lesson: concurrency bugs are the hardest class of bugs to debug. The only defense is not allowing the race condition to exist in the first place — via `synchronized`, `AtomicLong`, or a lock.

2. `volatile` ensures visibility (the write becomes visible to other threads), but `count++` requires atomicity (the read-modify-write must be indivisible). The breakdown:

   ```java
   volatile long count = 0;
   count++;  // THREE operations:
   // 1. read current value from main memory
   // 2. add 1
   // 3. write back to main memory
   ```

   Race condition: Thread A and Thread B both read the same value (say, 100), both increment to 101, both write back 101. The second increment vanishes.

   `volatile` does not prevent this because:
   - It only guarantees that writes flush to main memory and reads see the latest value
   - It does not make the read-modify-write atomic
   - The three steps can still be interleaved

   The fix — use `AtomicLong`, `synchronized`, or a lock to make the entire read-modify-write indivisible:

   ```java
   volatile long count = 0;               // Still broken! Need atomicity
   AtomicLong count = new AtomicLong(0);  // Correct
   count.incrementAndGet();               // Atomic CAS (Compare-And-Swap)
   ```

3. A visibility test that spins waiting for a flag to change:

   ```java
   boolean done = false;  // No volatile

   // Thread 1
   done = true;

   // Thread 2
   while (!done) {  // Infinite spin if flag not visible
       // empty loop
   }
   ```

   Without the fix it spins forever (compiler optimizes to an infinite loop). Add `System.out.println` inside the loop and it starts passing, even without the fix:

   ```java
   while (!done) {
       System.out.println("waiting...");  // <-- Magic "fix"
   }
   ```

   Why:
   - `System.out.println` calls synchronized methods internally (`PrintStream` is synchronized)
   - Synchronized blocks create happens-before edges (memory barriers)
   - Each iteration now acquires/releases a monitor, which forces the JVM to re-check the `done` flag from main memory
   - The flag becomes visible, and the loop exits

   What this tells you about "it works on my machine":
   - Concurrency bugs are timing-dependent. Adding logging, debuggers, sleeps, or lock contention changes thread scheduling
   - Instrumentation can mask bugs. A race condition that fails under load might "pass" when you add logging, because the pauses serialize access
   - You cannot trust local testing. A test that passes in dev fails in production under real load with many threads
   - The rule: don't make it volatile, don't add println — fix the race properly (`volatile` for visibility, a lock/atomic for atomicity)

**The trade-off in one line:** Every happens-before edge (synchronized, volatile, thread start/join) is a memory barrier that constrains the CPU and compiler — correctness under concurrency is bought with performance.

**Interview angle:** Adding println "fixed" it because println internally uses synchronized, which created a memory barrier. That's proof the bug was visibility, not logic. In production, we remove that crutch and use volatile or a proper synchronization mechanism. The bug doesn't disappear; it's just hidden until load testing.

**Still fuzzy:**

---

## Day 5 - Four ways to count, and what each costs

**Date:** 16th September, 2026 | **Time spent:** 22 Minutes

**What I built:** 3 Copies of a counter program, implementing different ways to ensure atomicity and volatility

**The three questions:**

1. Measured ops/sec at 8 threads:

   | Strategy | Ops/sec |
   |---|---|
   | `synchronized` | 5,632,910 |
   | `ReentrantLock` | 32,506,054 |
   | `AtomicLong` | 63,544,512 |
   | `LongAdder` | 411,374,505 |

   Yes, this is the ordering I expected:
   - `LongAdder` — fastest. Threads spread writes across an array of cells (striping), so contention on any single cache line is rare.
   - `AtomicLong` — fast at low/moderate contention, but degrades under high contention. Every thread CAS-es the same cache line, so retries and cache-line ping-pong between cores dominate at 8 threads.
   - `ReentrantLock` — similar cost profile to `synchronized`; slightly more overhead unless you need its extra features (`tryLock`, fairness, interruptibility).
   - `synchronized` — slowest under load. Once contended, the JVM inflates the lock and threads park in the kernel — a contended acquire costs microseconds instead of nanoseconds.

2. Yes, likely — `AtomicLong` may pull ahead of or match `LongAdder`.
   - At 2 threads, CAS retries on `AtomicLong` are rare — most compare-and-swap attempts succeed on the first try since only two threads compete for one cache line.
   - `LongAdder`'s striping (array of cells) adds overhead for no benefit when there's little contention to relieve — you pay for cell allocation and the `sum()` aggregation cost without the payoff of avoiding cache-line ping-pong.
   - The lesson: `LongAdder`'s advantage only shows up when contention is real. At low thread counts, plain `AtomicLong` is simpler and just as fast (or faster) because there's nothing to shard.

3. `LongAdder`

**The trade-off in one line:** `LongAdder` trades read cost and atomic-snapshot semantics for write throughput.

**Interview angle:** "For a scraped metrics counter, `LongAdder` is the right call — I'm trading a consistent snapshot for write throughput, which is the correct trade because nothing here reads-and-acts on the exact value. If this counter gated a business decision instead of feeding a dashboard, I'd need `AtomicLong`'s real atomicity instead."

**Still fuzzy:**

---

## Day 6 - Virtual Threads

**Date:** 17th September, 2026 | **Time spent:** 10 Min

**What I built:** A simple program to see diff between platform threads and virtual threads (its massive)

**The three questions:**

1. Speedup was about ~22.7x:
   - Platform threads: `tasks / poolSize` rounds × `taskDuration` = 5,000 / 50 = 100 rounds × 20 ms = 2,000 ms.
   - Virtual threads: theoretically should approach one task duration (~20 ms), since all 5,000 can be "in flight" simultaneously and none compete for a scarce carrier while blocked.

   So the theoretical ratio is 100 rounds → you'd expect something closer to 100x. Measured 22.7x — lower than theoretical, because:
   - Virtual threads have real (if small) overhead: creating 5,000 continuations, mounting/unmounting them on carrier threads, and the scheduler coordinating all of it isn't free — hence 91 ms instead of the theoretical 20 ms.
   - The platform pool also isn't a perfect 2,000 ms floor — real sleep/scheduling jitter adds a bit.

   The core relationship: the speedup ratio is fundamentally `poolSize`-bound for platform threads — you're trading a hard ceiling (`tasks/poolSize` rounds) for a near-constant cost when using virtual threads.

2. Measured: platform 244 ms vs. virtual 278 ms (virtual threads slightly slower).

   Prediction (before running): no speedup for virtual threads — possibly even a small loss — because a CPU busy loop never blocks. Virtual threads only help when a task blocks and unmounts from its carrier, freeing that carrier for other work. A pure CPU-bound task occupies a carrier (and a core) for its entire duration regardless of thread type.

   Was I right? Yes — the data confirms it. Virtual threads still get scheduled onto a small number of carrier threads (roughly one per core), so CPU-bound work sees the same core-bound ceiling as platform threads, plus the small extra cost of virtual-thread bookkeeping (mounting/unmounting machinery) that has no payoff here — hence 278 ms vs. 244 ms.

   The rule this confirms: virtual threads are for blocking, I/O-bound work only. For CPU-bound work, a pool sized to core count is still the right model — virtual threads add zero benefit and a small tax.

3. Before (platform threads): my 50-thread pool accidentally capped concurrent calls to the downstream API at 50 in-flight requests. Even if each call takes 100 ms, that's `50 / 0.1s` = 500 req/s max — coincidentally right at the downstream's limit. The pool size was acting as an implicit rate limiter.

   After (virtual threads): there's no pool size to bound concurrency — thousands of virtual threads can all call the downstream API at once. Nothing stops you from firing far more than 500 req/s and overwhelming it.

   What I must add explicitly: a rate limiter or semaphore sized to the downstream's real capacity — e.g.:

   ```java
   Semaphore downstreamLimit = new Semaphore(500); // or a proper token-bucket limiter

   downstreamLimit.acquire();
   try {
       callDownstreamApi();
   } finally {
       downstreamLimit.release();
   }
   ```

**The trade-off in one line:** Virtual threads make blocking free and thread-per-request scale to hundreds of thousands of connections, but they buy nothing for CPU-bound work and remove the accidental rate limiter your pool size used to provide.

**Interview angle:** With virtual threads, thread-per-request scales to hundreds of thousands of concurrent connections, so the async complexity is no longer the price of concurrency — but blocking downstream calls are still not free for the downstream service, so I'd put an explicit semaphore or rate limiter exactly where the thread pool used to be doing that job by accident.

**Still fuzzy:**