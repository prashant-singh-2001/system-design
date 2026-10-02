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

### Template

Copy this for each day.

```
### Day N - <title>

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

### Day 1 - The memory hierarchy, measured

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

### Day 2 - Back-of-the-envelope estimation

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

### Day 3 - Little's Law and the queueing knee

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

### Day 4 - The JVM memory model

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

### Day 5 - Four ways to count, and what each costs

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

### Day 6 - Virtual Threads

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

---

### Day 7 - Blocking vs non-blocking I/O

**Date:** 18th September, 2026 | **Time spent:** 32 Minutes

**What I built:** NIO Server to compare and contrast to simple blocking echo server

**The three questions:**

1. `BlockingEchoServer` is ~95 lines; `NioEchoServer` is ~170+ lines — roughly 1.8x longer, and that's before accounting for the fact that the Selector version still skips partial-write handling (the Stretch goal), which would add more.

   Where the extra complexity went:
   - Explicit state machine instead of implicit control flow. `BlockingEchoServer.handle()` is a single method with a while loop that reads a line and writes it back — the call stack itself tracks "where we are" in the conversation. `NioEchoServer` has no equivalent single place: a connection's life is split across `handleAccept` (birth), `handleRead` (every subsequent event), and cleanup scattered wherever `-1` or an exception is detected.
   - Manual lifecycle management. You must remember to flip channels non-blocking, register/cancel keys, and clear `selectedKeys()` every pass — none of which the blocking model requires, because the OS thread scheduler was already doing the equivalent bookkeeping for you.
   - A single point of failure for all connections. The blocking model isolates failures per-thread (one connection's exception doesn't touch another's stack). The Selector version needs a try/catch inside the loop, per key, or one bad channel takes down every connection on that thread.
   - Buffer and protocol state has to live somewhere explicit (this sets up Question 2) — the blocking version gets that for free from local variables on the stack.

2. Where it lives now: nowhere, really — this exercise's `handleRead` only echoes whatever bytes arrived in a single read, so there's no state to carry between events. That's exactly why it's simpler than a real protocol implementation.

   But for a protocol that needs partial-message buffering (e.g., a newline-delimited protocol like the blocking version's `readLine()`, where a message might arrive across multiple `OP_READ` events): you'd need to keep a per-connection buffer alive between selector wakeups, since there's no call stack to hold it for you.

   The mechanism: `SelectionKey.attach(Object)` / `key.attachment()`. You'd create a small state object per accepted connection:

   ```java
   class ConnectionState {
       ByteBuffer pending = ByteBuffer.allocate(1024);
       // could also hold: parse position, message-so-far, protocol phase, etc.
   }
   ```

   Attach it when you register the channel:

   ```java
   SelectionKey readKey = clientChannel.register(selector, SelectionKey.OP_READ);
   readKey.attach(new ConnectionState());
   ```

   Then in `handleRead`, retrieve it with `key.attachment()`, append newly-read bytes into `pending`, scan for a complete message (e.g., a `\n`), and only act once you have one — leaving any leftover bytes in the buffer for the next event.

   This is the core trade-off of Selector-based I/O: the thread scheduler isn't holding your state anymore, so `SelectionKey` becomes your only hook for "whatever this connection needs to remember between events."

3. One case where I would: building a proxy or load balancer that only forwards bytes and never inspects payload (e.g., a raw TCP/TLS passthrough proxy). Here you genuinely benefit from fine-grained control over buffers and backpressure — you're moving bytes between two channels without ever needing "connection state" in the business-logic sense, so the extra complexity buys real control over memory and flow, and virtual threads offer no real advantage since there's no blocking business logic to simplify.

   One case where I would not: a typical REST/RPC service that calls a database and a couple of downstream APIs per request. Here, virtual threads let me write plain sequential blocking code — `Connection conn = dataSource.getConnection(); ...` — and get the same scalability the Selector model offers, without hand-rolling a state machine, without `SelectionKey` attachments, and with stack traces and debuggers that actually work. The Selector model's cost (explicit state management, no free thread-per-connection isolation) buys nothing here that virtual threads don't already give me for free.

**The trade-off in one line:** Virtual threads give blocking code non-blocking-level scalability, so the Selector's extra complexity is now only worth paying for byte-only proxies, fine-grained buffer/backpressure control, or runtimes without virtual threads.

**Interview angle:** If asked to handle 100,000 concurrent connections, the strong answer names both paths and picks on evidence: "either an event loop, or virtual threads with blocking code — I would start with the second because it is far easier to debug, and move to an event loop only if profiling showed the scheduler was the bottleneck."

**Still fuzzy:** 

---

### Day 8 - HTTP, TCP, and what a connection costs

**Date:** 19th September, 2026 | **Time spent:** 30 Minutes

**What I built:** Small server that accepts 3 kinds of request

**The three questions:**

1. Measured on loopback: ~0.6 ms per-request difference between a fresh connection and a reused one (consistent with Day 1's "same datacenter: ~0.5 ms round trip, handshake ~0.5 ms" — the gap is one handshake RTT).

   Scaling to a cross-country round trip (~40 ms): the per-request overhead doesn't scale from the small loopback number — it becomes the new RTT directly, since the gap is fundamentally "one handshake's round trip," and RTT is what changed:

   `Per-request extra cost ≈ 40 ms`

   For a 300-request page load, opening a fresh connection each time:

   `300 × 40 ms = 12,000 ms = 12 seconds`

   versus a reused connection, which pays that 40 ms exactly once for the whole page.

   What this does to a page load: 12 seconds of pure connection-handshake overhead, before any actual response bytes move — completely unacceptable for a single page. This is the entire justification for connection pooling, HTTP keep-alive, and HTTP/2 multiplexing.

2. What `createContext("/")` gives you: a single mechanism — "does this path start with X" — with one fixed handler per registered prefix.

   What a real router adds:
   - Method-aware routing (same path, different handler per `GET`/`POST`/`DELETE`)
   - Path variables (`/users/{id}`) extracted as typed parameters
   - Deterministic most-specific-match resolution instead of first-prefix-wins
   - Middleware/interceptor chains (auth, logging, CORS) composed around the handler
   - Content negotiation and request body parsing/validation integrated into dispatch

   What it costs:
   - More CPU per request (walking a compiled routing structure — often a trie — plus running a middleware chain), though still microseconds at normal scale
   - More memory and startup time (building/compiling the routing table)
   - Harder debugging (which route matched, what order middleware ran)

   The core insight: prefix matching already is the routing algorithm's essence — everything a real router adds is refinement (sharper match criteria) and composition (chained behavior around the match), not a different idea.

3. Little's Law (Day 3): `λ = L / W`

   `λ = 10 connections / 0.05 s = 200 req/s`

   Maximum throughput: 200 requests per second. Beyond that, requests queue for a free connection regardless of how fast the downstream server responds — the pool size is the hard ceiling, exactly like the platform-thread pool ceiling from Day 3/6.

**The trade-off in one line:** Pooled connections hold resources on both ends and go stale. A pooled connection to a server that has silently gone away fails on first use, which is why pools need validation queries and idle timeouts, and why "connection reset" is such a common production error.

**Interview angle:** When you draw a service calling three others, say "I would use pooled, keep-alive connections here — the handshake is a full round trip and at this QPS that is real latency." It shows you are costing the arrows on your diagram, not just drawing them.

**Still fuzzy:**

---

### Day 9 - Wire formats

**Date:** 20th September, 2026 | **Time spent:** 32 Min

**What I built:** Two codec

**The three questions:**

1. Measured sizes:

   | Format | Size | Ratio to binary |
   |---|---|---|
   | Binary | 75 bytes | 1.0x |
   | JSON | 133 bytes | 1.8x |
   | Java serialization | 182 bytes | 2.4x |

   Extra bytes per event using JSON instead of binary: `133 − 75 = 58 bytes`

   At 1M events/sec:
   - `58 bytes × 1,000,000 events/s = 58,000,000 bytes/s ≈ 58 MB/s` of pure waste
   - Per day: `58 MB/s × 86,400 s ≈ 5.0 TB/day`
   - Per year: `5.0 TB/day × 365 ≈ 1.83 PB/year`

   That's the real number: ~1.8 petabytes of extra annual bandwidth just from choosing JSON over binary at this event rate. That's the kind of number that turns "binary is smaller" from a nice-to-know into a line item a VP would ask about.

2. You prefix each field with a small numeric tag identifying which field it is, plus enough information for a reader to know how many bytes to skip even if it doesn't recognize the tag:

   ```
   [tag=1][value: id]
   [tag=2][value: type]
   [tag=3][value: timestampMillis]
   [tag=4][value: payload]
   [tag=5][value: newField]   <- added later
   ```

   Why this solves the problem completely:
   - An old reader that doesn't know about tag 5 simply doesn't have a case for it in its switch statement — but because the field is length-prefixed (or a fixed known size for its type), the old reader can skip exactly that many bytes and keep reading the fields it does understand. Nothing breaks.
   - A new reader given an old message (no tag 5 present) just never sees that tag and uses a default value for the new field.
   - Field order no longer matters at all — you could write tag 4 before tag 2 and it'd still decode correctly, because the reader identifies fields by tag, not position.

   This is literally Protobuf's wire format: every field is `(tag_number << 3 | wire_type)` followed by a value, where the wire type (varint, length-delimited, fixed32/64) tells any reader — even one that's never heard of that tag — exactly how many bytes to skip. That's the one rule that makes "add a field" safe and "the format evolves" possible, which is the whole reason Protobuf/Avro/Thrift exist instead of everyone hand-rolling `BinaryCodec`-style positional formats.

3. Public API → JSON. Driven by two things: (1) you don't control the clients — any language must be able to read it, and you can't force every external consumer to redeploy in lockstep with you, so "adding a field doesn't break old readers" matters enormously; (2) humans need to debug it — `curl` and `cat` should show you something readable.

   Internal event bus → compact binary (in practice, Protobuf/Avro, not a raw positional format). Driven by: (1) you control both ends, so you can coordinate schema/version changes across producer and consumer without breaking anyone external; (2) at high volume, the bandwidth and CPU savings are real money — repeated field names and text-encoded numbers are pure waste at a million events/second, as the Q1 math just showed.

**The trade-off in one line:** You're choosing between bytes on the wire and the ability to change your mind later — internal high-volume paths can afford a schema and its coordination cost, public APIs usually can't.

**Interview angle:** "JSON at the edge for compatibility and debuggability, Protobuf or Avro internally for density and schema evolution" is the answer — but only convincing when you can say *why* each side needs what it needs, not as a memorised pairing.

**Still fuzzy:**

---

### Day 10 - Phase review: estimate a feed

**Date:** 21st September, 2026 | **Time spent:** 34 Min

**What I built:** Feed Estimator

**The three questions:**

1. ~100,000 followers, defended with actual numbers rather than a feeling:

   Assume a dedicated fanout-worker pool — say 10 workers, each capable of ~5,000 writes/s (the same per-server throughput figure used for app servers in this exercise), giving an aggregate fanout capacity of `10 × 5,000 = 50,000 writes/s`.

   For a single post's fanout to complete within a reasonable propagation window — say 2 seconds, so it doesn't back up behind the next post from the same or another account — the followers that pool can absorb in that window is:

   `50,000 writes/s × 2s = 100,000 followers`

   Above that, a single post's fanout either takes uncomfortably long (queues build up, followers see the post late) or requires proportionally more dedicated capacity just to serve outlier accounts — capacity that sits mostly idle the rest of the time. That's the actual trade being made: below ~100,000 followers, fanout-on-write finishes fast enough to be invisible; above it, the write burst from one post becomes disproportionate to the value of serving that one account via the write path.

2. If the real distribution is 99/1 (more skewed than 80/20):

   Counterintuitively, your total cache capacity is actually over-provisioned — you'd only need to cache the top ~1% of items to capture 99% of traffic, far less memory than the 20% you sized for. So raw capacity isn't what breaks.

   What does break is hot-key/hot-partition load: extreme skew concentrates enormous traffic onto a tiny number of individual items. If those few ultra-hot items land on the same cache shard or node, that single node gets overwhelmed regardless of how much total cluster capacity you have — the same "shard the hot resource" lesson from Day 6 (`LongAdder`), just one level up the stack. Aggregate sizing looks fine on paper while one node melts under real traffic.

   If the real distribution is 50/50 (traffic roughly uniform, not skewed at all):

   Here the opposite problem hits: caching only 20% of the dataset only captures roughly 20% of traffic, not 80%, because there's no concentration to exploit — every item is about equally likely to be requested. Your hit rate collapses, most reads fall through to the backing store, and the cache stops paying for itself. In this regime you either need to cache almost everything (expensive) or abandon caching as the primary strategy and lean on read replicas / horizontal read scaling of the backing store instead — a fundamentally different architecture.

   The takeaway: the 80/20 rule is a measured assumption, not a law of nature. It directly decides both how much cache memory to buy and whether caching is even the right lever to pull — so in a real system you'd validate it against an actual access-frequency histogram before sizing anything.

3. I'd stop treating "writes" as one undifferentiated number.

   Day 2 computed write QPS as 4,000/s — the rate of users creating posts (the logical write rate). But Day 10 reveals that once fanout-on-write is in the picture, the physical write rate against the feed-storage layer is 800,000/s — 200x higher. Those are two different numbers answering two different architectural questions, and Day 2's estimate only captured one of them.

   Concretely, I'd now split the estimate into:
   - Primary write rate (4,000/s) — what sizes the posts table / source-of-truth storage.
   - Fanout write rate (800,000/s) — what actually sizes the feed-cache/index layer, and what tells you whether a single database can handle writes at all or whether you need sharding, queuing, or a hybrid strategy from day one.

   Day 2's storage estimate also only accounted for primary post records (300 bytes × writes/day) — it never accounted for the denormalized feed-index copies fanout-on-write creates, which could be a much larger storage line item depending on whether feeds store full copies or lightweight references. I'd now estimate primary storage and feed-index storage as two separate rows, not one combined "storage per year" figure — because they're sized by completely different quantities (post count vs. post-count × average-followers).

**The trade-off in one line:** Fanout-on-write and fanout-on-read each have real downsides, so the hybrid combines them — at the cost of two code paths and a threshold to tune, forever.

**Interview angle:** The fanout number is the pivot of any feed question. Get to it early: "before I choose fanout-on-write, let me check the write amplification — 4,000 posts/s times 200 average followers is 800,000 writes/s, which is significant but tractable. The problem is the tail: a 50-million-follower account makes one post into 50 million writes. So I would go hybrid." That is a complete, defensible design position in about twenty seconds.

**Still fuzzy:**

---

## Phase 1 retrospective

- **Three numbers I'll still remember in six months:** 58 extra bytes per event (JSON vs. binary) compounds to 5 TB/day at 1M events/sec; a 15% rounding error in an estimate can be noise or can flip a decision, depending on how close you are to the threshold; virtual threads gave at least a 22x speedup on blocking I/O, but zero benefit on CPU-bound work.
- **The one idea that genuinely surprised me:** how much cheaper binary is than JSON (1.8x smaller) even though JSON is paying for something real — field names on the wire buy you human-readability and schema flexibility, not nothing. The gap felt larger than the "convenience tax" I expected.
- **Still fuzzy, carrying into Phase 2:** [name the specific thing here, e.g. "when exactly connection-pool validation queries fire" or "the mechanics of field-tag skipping in Protobuf's wire format" — a generic "some questions" won't be retrievable in six months, which defeats the point of writing it down]

---

### Day 11 - Single Responsibility

**Date:** 22nd September, 2026 | **Time spent:** 

**What I built:** Split `LegacyOrderProcessor` into `PricingService`, `OrderValidator`, and a coordinating `OrderProcessor`

**The three questions:**

1. New (`PricingService`): 2 lines of setup — `new PricingService()` plus a sample `Order`. No fakes, no infrastructure, and each calculation step (`subtotalCents`, `discountedSubtotalCents`, `totalCents`) is independently assertable.

   Legacy (`LegacyOrderProcessor`): can't test pricing in isolation at all — `process()` bundles validation, pricing, persistence, and notification into one call. To check a tax calculation you'd need an `Order` that satisfies every validation rule, and you'd only ever see the final `total` — subtotal, discount, tax, and shipping are local variables, never individually observable. In a real system (not this toy version's in-memory fakes) that also means standing up a real database and email server just to check tax math.

   The real difference isn't line count — it's coupling and observability. The old design forces you through four unrelated concerns to test one; the new design isolates the one thing you're testing and exposes every intermediate step.

2. | Change | File(s) touched |
   |---|---|
   | Tax rate → 22% | `PricingService.java` only |
   | Switch to DynamoDB | A new `OrderRepository` implementation only — the interface and `OrderProcessor` are untouched |
   | Confirmations → SMS | A new `ConfirmationSender` implementation only |

   `OrderProcessor` changes in none of these cases — that's the payoff of constructor-injecting the validator, pricing service, and both ports as abstractions rather than concrete logic. Each of the four forces of change (rules, finance, platform, marketing) now has exactly one file that's theirs to touch.

3. No — calling four collaborators isn't the same as having four reasons to change. SRP is about forces that can independently demand a change, not dependency count.

   `OrderProcessor`'s single reason to change is the shape of the workflow itself — adding a step (e.g. a fraud check before pricing), reordering steps, or adding workflow-level concerns like a rollback if persistence fails after pricing succeeds. That's distinct from *what* validation checks, *how* tax is computed, *where* orders are stored, or *how* confirmations are sent — none of which touch this class. It "does four things" only in the trivial sense of calling four methods; it has exactly one reason to change.

**The trade-off in one line:** Splitting buys testability and isolates each team's changes, but costs more files and indirection — split along the lines where change actually arrives, not on principle alone.

**Interview angle:** "What would have to change for this class to change?" is a sharper design-review question than "does this class do one thing?" — it produces an answer you can act on, like the file-touch table above.

**Still fuzzy:**

---

### Day 12 - Open/Closed

**Date:** 23rd September, 2026 | **Time spent:** 22 Minutes

**What I built:** A shipment calculator with independent carrier strategies — the calculator doesn't have to be modified to add a new carrier

**The three questions:**

1. No files were touched apart from registering `DRONE` itself — it's added inline as a lambda, with zero edits to any existing carrier or to `ShippingCalculator`. In the legacy design, adding `DRONE` would require editing `LegacyShippingCalculator.quoteCents()` directly — a new `else if` branch in the same shared method every other carrier lives in, forcing a re-review and re-test of the whole file just to add one case.

2. In my gamified tracker project, there are several types of logs, but the set is fixed and closed — new log types don't arrive from outside my control the way a new shipping carrier arrives from a partner integration. A strategy registry there would be over-engineering: I'd be paying the "five files instead of one" cost for an axis of change that isn't actually moving. What distinguishes it from the carrier case is who can demand a new case and how often — carriers are added by external partners on their own schedule; my log types are added (rarely) by me, deliberately, with no unpredictable external pressure. When I can confidently say the set is closed and I control it, a plain `if/else` or `switch` stays correct.

3. In a real application, the registry is populated one of three ways, each trading something different:
   - Hard-coded defaults (what `withDefaults()` does here) — simple and type-safe, but a new carrier still needs a code change and redeploy, just a safer, additive one.
   - A config file mapping carrier name to implementation — lets ops add a carrier without a code deploy, and supports different carrier sets per tenant/region, but loses compile-time safety (a typo'd class name fails at runtime) and usually needs reflection to wire up.
   - Classpath scanning (e.g. an annotation the framework scans for at startup) — true zero-touch extension, drop a JAR in and the carrier appears, but it's "magic": harder to trace where a carrier actually gets registered by reading code, and startup pays a scanning cost.

**The trade-off in one line:** OCP buys additive extension — write a class, register it — but spreads logic across five files instead of one; worth paying only at the axis of change you actually expect, not by default everywhere.

**Interview angle:** Phrase it around the axis of change: "I expect new carriers, so I make carrier a strategy. I don't expect new order states, so those stay an enum with a switch." That shows you're choosing per case, not applying OCP as a blanket rule.

**Still fuzzy:**


---

### Day 13 - Liskov Substitution

**Date:** 24th September, 2026 | **Time spent:** 12 Minutes

**What I built:** Bounded Store which stores latest entries to a capacity and is easy to extend.

**The three questions:**

1. What else would have caught the silent write drop before production, other than the contract test:
   - A round-trip integration test — write a value, then immediately read it back and assert equality, run under a scenario that fills the store past capacity. This would have caught it directly, no contract or interface change needed.
   - A code-review checklist item — "does every `put`-like call site check its return value / handle failure?"
   - Production monitoring — a metric comparing "writes attempted" vs. "keys present in store" would surface silent drops immediately.
   - Static analysis — a linter flagging ignored non-void return values, though this only helps *after* `put` is changed to return something; a `void put(...)` gives it nothing to flag.

2. Signature change: `void put(K, V)` → `boolean put(K, V)`, returning `false` when the store is full and the write is rejected.

   This mirrors a real Java precedent: `Queue.add()` throws when full ("capacity exceeded is exceptional"), while `Queue.offer()` returns `false` ("capacity exceeded is a routine, expected outcome"). A bounded store being full is exactly the latter — it's the entire point of being bounded, not an exceptional condition.

   I prefer the boolean-return design: `false` gives the caller *information* to act on (evict something, retry, log, drop silently on purpose), whereas an exception forces every call site into catch-and-handle for something that will happen constantly by design.

3. Yes — this is a Liskov violation, and the compiler cannot catch it.

   `Optional<String> get(String)` carries an implicit contract: callers always receive a non-null `Optional`, which they can safely call `.isPresent()`, `.map(...)`, or `.orElse(...)` on without ever null-checking the `Optional` itself — that's the entire reason `Optional` exists.

   A subtype returning literal `null` instead of `Optional.empty()` weakens that postcondition. It compiles perfectly — `null` is assignable to any reference type — but any caller who correctly trusted the contract (chaining `.map()` or calling `.isPresent()` without a null guard) gets a `NullPointerException` the moment this implementation is substituted in. That's exactly what the concept section warns about: "the compiler enforces the SHAPE of an interface. It cannot enforce the BEHAVIOUR." The shape (`Optional<String>`) is honored; the behavioral promise (non-null, always) is not.

   A contract test would catch this in one line (`assertThat(store.get("missing-key")).isNotNull()`) and hold every implementation, present and future, to it automatically.

**The trade-off in one line:** A precise contract constrains implementers — say too much and you rule out legitimate implementations (e.g. "all keys retained forever" would ban bounded stores entirely); say too little and substitutability means nothing. Getting that line right is genuine design work.

**Interview angle:** The square/rectangle example is fine but abstract — a sharper answer names a real incident: "our read-through cache implemented the repository interface but returned stale data, so callers relying on read-your-writes broke; we wrote a contract test for the interface and ran it against every implementation." That's Liskov as an operational concern, which is what it actually is.

**Still fuzzy:**

---

### Day 14 - Interface Segregation

**Date:** 28th of September, 2026 | **Time spent:** 25 Minutes

**What I built:** Segregated a heavy User Repo interface into role specific interfaces.

**The three questions:**

1. Before: 12 methods (`LegacyUserRepository`). After: 4 methods (`UserReader`) — `findById`, `findByEmail`, `findAllActive`, `count`. Nothing about writes, admin, or maintenance needs to be implemented or even thought about.

2. The exact mechanism: a fat interface (`LegacyUserRepository`) forces every implementor to provide a body for all twelve methods, regardless of role. A legitimate read-only implementor (`ReadOnlyUserCache`) has no sensible behavior for `vacuum()` — there's nothing to vacuum in an in-memory cache. Java requires the method be implemented anyway, so the only options are to throw (`UnsupportedOperationException`) or fake success with a no-op. Either one breaks Liskov: the supertype implicitly promises "calling any of these twelve methods succeeds," and a caller holding a `LegacyUserRepository` reference who calls `.vacuum()` reasonably expects it to work — but this implementation throws where success was promised. So the chain is: **fat interface → forces implementation of irrelevant methods → no sensible behavior exists for them → throwing/faking to satisfy the compiler → substitutability breaks.** ISP violations don't just correlate with LSP violations, they cause them, because segregation removes the irrelevant method from the type entirely instead of leaving it to be implemented badly.

3. Take `UserReader` and `EmailSender` as two separate constructor dependencies, not one `UserService` facade — unless reading-the-user-then-emailing-them is itself a single cohesive, atomic operation in your domain (e.g. it needs its own transactional or rate-limiting guarantees), in which case a purpose-built facade for *that specific operation* is fine.

   **What decides it:** whether the two capabilities are cohesive as one operation, or just two unrelated things a class happens to need. Bundling them into one facade by default recreates the exact fat-interface problem from today's exercise — every consumer that only needs to read users would also depend on email-sending capability, and vice versa, and a test double for one concern would have to stub the other. Taking both dependencies separately keeps each role visible at the type level: a component holding only a `UserReader` provably cannot send email.

**The trade-off in one line:** More interfaces to name and longer `implements` clauses for components that legitimately do everything — modest costs, but segregate too finely and you get a dozen single-method interfaces nobody can keep straight.

**Interview angle:** "We split the repository by role, so the read path takes a `UserReader` and literally cannot write — it stopped being a code-review rule and became a compile error." A specific, checkable outcome is what makes that answer credible.

**Still fuzzy:**

---


### Day 15 - Dependency Inversion

**Date:** 29th September, 2026 | **Time spent:** 29 Minutes

**What I built:** Deconstructed a JDBC-direct calculating service into a purely arithmetic service depending on just a list of sale objects

**The three questions:**

1. DIP's actual mechanism is: the high-level module depends on an abstraction it owns, not a concrete implementation it doesn't. "Testability" isn't a separate payoff of that mechanism — it's the exact same substitutability, just exercised at a different time and for a different reason.

   Swapping `JdbcSalesDataSource` for `() -> SALES` in a test and swapping it for `CsvSalesDataSource` or `HttpSalesDataSource` in production are the same operation: replacing whatever sits behind `SalesDataSource` without touching `ReportService`. Test-time substitution just happens to be the substitution you exercise constantly (every test run) and the one you notice first, because it's immediate and free of infrastructure — but it's not a different capability from the "swap Postgres for DynamoDB" flexibility the concept section describes. If you can plug in a fake for testing, you can, by the same mechanism, plug in a different real implementation for deployment. They're one axis — pluggability through an owned abstraction — described from two vantage points.

2. The problem: `List<Sale> findSales()` forces the entire result set to materialize in memory before `ReportService` can do anything — at 10 million rows, that's a serious memory/GC problem, and there's no way to start processing before the whole list loads.

   What I'd change: the port's return type, to something that streams rather than materializes everything at once — `Stream<Sale> findSales()` is the natural JDK choice (lazy, closeable, short-circuitable), or a cursor/pagination-based signature (`List<Sale> findSales(int offset, int limit)`) if the caller needs explicit control over batching.

   Does this threaten the abstraction? Only partially, and it's worth being precise about which part:
   - The port itself survives cleanly. `Stream<Sale>` is still a general-purpose JDK type with zero JDBC leakage — no `ResultSet`, no `Connection`, no `SQLException` — so the "nothing about persistence may leak into the port" rule from the concept section still holds.
   - But the caller's contract genuinely changes. A `Stream` can only be consumed once and must be closed (try-with-resources), so `ReportService`'s grouping/sorting logic has to adapt from simple list operations to stream-based ones, possibly needing to collect into intermediate structures if it needs multiple passes over the data.
   - The deeper tension: at real scale, there's pressure to push the grouping/summing itself down into the data source (e.g., a DB-side `GROUP BY SUM`) rather than doing it in `ReportService`. That would be faster, but it re-couples domain logic (how we report on sales) with a specific infrastructure capability (SQL aggregation) — exactly the coupling DIP was introduced to remove. So the honest answer is: the interface can evolve without breaking the abstraction, but the temptation the scale creates is real, and giving in to it would undo the inversion.

3. With `ReportService` — the domain side — not with the JDBC implementation.

   This is the literal meaning of "the domain writes the job description; infrastructure applies for the job" from the concept section: the port belongs to whoever needs it, and the implementation depends on the port's location, never the reverse.

   Why it matters, concretely: package/module placement is what makes the dependency direction physically enforceable, not just a naming convention. If `SalesDataSource` lived in the JDBC package instead:
   - `ReportService` (pure domain arithmetic) would need the JDBC module on its classpath just to see the interface, even though the interface's signature has nothing JDBC-specific in it.
   - Build tooling would show a dependency edge from domain → infrastructure, which is precisely the wrong-direction dependency DIP exists to eliminate.
   - You couldn't compile, test, or ship the domain logic in isolation — you'd always be dragging the JDBC module along.
   - Adding `CsvSalesDataSource` (the Stretch goal) would awkwardly require depending on the JDBC module too, purely to reach the interface it's supposed to be an alternative to.

   Placing the port with the domain makes the inversion physically real, not just conceptual: infrastructure modules depend on the domain module (to implement its ports), and the domain module depends on nothing infrastructure-related. That's a rule a build tool — or an architecture fitness function, which is exactly what Day 17's hexagonal architecture will formalize — can mechanically verify: "the domain package imports nothing from any infrastructure package."

**The trade-off in one line:** An extra interface per boundary, and the wiring has to happen somewhere (a composition root or DI container) — worth it at real boundaries like storage, network, clock, and randomness, but ceremony rather than design for a pure function you'll never replace.

**Interview angle:** "The domain declares the port and infrastructure implements it, so the dependency arrow points inward." It sets up Day 17's hexagonal architecture and signals that you understand the direction, not just the indirection.

**Still fuzzy:**

---

### Day 16 - Value objects and immutability

**Date:** 30th September, 2026 | **Time spent:** 19 Minutes

**What I built:** A `Money` type with immutable, currency-checked arithmetic (backed by a record) that avoids floating point entirely, plus a `Basket` that defensively copies on both the way in and the way out

**The three questions:**

1. The invariant: for any amount split into N parts, the parts must sum back to exactly the original amount, and no two parts may differ by more than one minor unit — divide evenly, then hand out the remainder one minor unit at a time.

   It must hold for every input, not just the ones tested, because money can't be created or destroyed by an allocation. If it holds for typical amounts but silently breaks on an edge case (a small remainder, an amount smaller than the number of parts), that edge case doesn't stay isolated — it gets summed into a larger invoice or ledger total, and the discrepancy only becomes visible once it's mixed into an aggregate figure someone else is trying to reconcile. That's exactly the "where did the penny go" scenario the concept section describes: correct-looking code that's silently wrong only sometimes is far more expensive to find than code that's obviously wrong always.

2. Without the copy (`this.items = items;` instead of `this.items = List.copyOf(items);`), `Basket` holds a reference to the *same list* the caller passed in. If that caller mutates the list later from some unrelated code path — a "remove out-of-stock item" step, a promo-code handler — the basket's contents change too, with zero calls made to `Basket` itself.

   In a log, this is the nasty part: since no method on `Basket` changed its state, there's no method call to log at all.

   ```
   10:15:02  Basket.total() -> £30.00
   10:15:09  Basket.total() -> £20.00
   ```

   The value changed "by itself," with nothing in between explaining it — no constructor call, no method invocation. Debugging means suspecting aliasing and hunting for every place holding a reference to the original list — the same "spooky action at a distance" class of bug as Day 4's visibility issues or Day 12's silent-drop bug: a state change with no causal trail.

3. Two real, defensible cases:
   - **The storage/wire boundary.** A database column or a JSON payload naturally represents an amount as a plain integer — store/transmit as `long`, but deserialize into `Money` the moment it crosses into domain logic, and unwrap only at the edge on the way back out. Same principle as Day 15's "nothing about persistence may leak into the port," applied in the other direction.
   - **A hot, high-throughput numeric loop where currency is already guaranteed uniform** — e.g. summing millions of minor-unit values in a batch settlement job already partitioned by currency upstream. The object-allocation cost of wrapping every intermediate value has no corresponding safety benefit here, since there's no realistic risk of mixing currencies.

   What's *not* a good reason: "we only handle one currency, so we don't need `Money`." That trades away the currency-mismatch protection for convenience, and the moment a second currency shows up, `Money` gets retrofitted under pressure instead of being there from day one — the same "axis of change you actually expect" judgment call as Day 12's OCP question.

**The trade-off in one line:** Defensive copying costs an allocation per call — nothing for a small basket, but measure it on a hot path over a large collection, and reach for a genuinely persistent data structure rather than abandoning the guarantee.

**Interview angle:** In any design touching money, saying "amounts are minor-unit integers with currency attached, never floating point" early costs three seconds and signals that you've shipped financial code.

**Still fuzzy:**

---

### Day 17 - Hexagonal architecture

**Date:** 1st October, 2026 | **Time spent:** 13 Minutes

**What I built:** `UrlShortener` whose core business logic lives in an independent domain package, with adapters and other supporting code kept outside it.

**The three questions:**

1. No *existing* file needs modification — but two things do change: a new adapter is added (e.g. `PostgresLinkRepository implements LinkRepository`), and the composition root (wherever `UrlShortener` is constructed and wired up) changes to pass in the new repository instead of `InMemoryLinkRepository`. What does **not** change is `UrlShortener.java` itself, the `LinkRepository` port it depends on, and `CodeGenerator`/`SequentialCodeGenerator`, since none of those know or care what's behind the repository. If the repository logic had instead been written directly inside `UrlShortener`, that file itself would have to be edited — which is exactly the dependency-pointing-the-wrong-way problem this architecture exists to prevent.

2. **The case for the repository:** the repository is the one place with full visibility into every stored URL, so checking "does this URL already exist" sits naturally next to `save`/`find`. More importantly, it's genuinely the safer place to guarantee *no duplicates under concurrency* — a domain-level "check then save" has a real race condition: two concurrent `shorten()` calls for the same URL can both pass `findByTargetUrl` before either `save()` commits, minting two codes for one URL. A repository backed by a unique constraint (e.g. a unique index on `target_url` with `INSERT ... ON CONFLICT`) is the only truly race-proof way to enforce that.

   **Why the domain is still the better home:** the idempotency rule — "return the existing link rather than minting a second code" — is a *business policy decision*, not a storage detail. It's the kind of rule a product owner could plausibly want to change (e.g. allow a second code per URL for a different marketing campaign), and that decision needs to live somewhere visible, in one place, not duplicated across every adapter (Postgres, CSV, HTTP) that would otherwise each need to reimplement it identically. The honest answer isn't strictly either/or: keep the idempotency *decision* in the domain, and add the uniqueness constraint in the repository anyway as a last-line-of-defense safety net against the race condition — belt and suspenders, not a substitute for each other.

3. I'd use a non-sequential code — either a random string (`SecureRandom` over the base-62 alphabet) or a hash derived from the URL (the Stretch goal's `HashCodeGenerator`), instead of an incrementing counter.

   **What that costs:** the collision-free guarantee the sequential counter gave for free disappears. With random codes, two different URLs can land on the same code, so you need a check-and-retry loop (generate, look it up, regenerate on collision) — more latency and more domain logic, since that retry has to live somewhere that can query the repository. The collision probability also isn't zero-sum with code length: as more codes get issued, collision probability grows roughly with the square of the count (the birthday problem), so keeping it acceptably low at scale means a longer code, trading away some of the compactness that made sequential codes attractive in the first place.

   A hash-derived code sidesteps the retry loop for the *same* URL (same input always hashes to the same code, which gives idempotency for free) but reintroduces the question the Stretch goal poses directly: what happens when two genuinely *different* URLs hash to the same truncated value? You still need an explicit collision-resolution policy — reject, chain, or extend the hash — there's no getting around deciding it somewhere.

**The trade-off in one line:** More packages and interfaces, even for a trivial CRUD endpoint that now spans three layers to do almost nothing — worth it when the domain is genuinely complex or infrastructure genuinely changes, ceremony otherwise.

**Interview angle:** "The domain declares ports and adapters implement them, so swapping storage touches no business logic" is a strong sentence in any design discussion — especially followed by an honest "for a CRUD service, I wouldn't bother."

**Still fuzzy:**

---

### Day 18 - Aggregates and invariants

**Date:** 3rd October, 2026 | **Time spent:** 22 Minutes

**What I built:** Order management system

**The three questions:**

1. Without an aggregate, each of the eight rules would be duplicated as ad-hoc checks across every service that touches order data directly — a checkout API, a cart service, an admin backoffice tool, a batch import job, a refund/cancellation service, a reporting/export job. Concretely:
   - Rule 2 (lines only added/removed while `DRAFT`) needs an `if (status == DRAFT)` guard before every single insert/delete on order lines, in every service that can reach that table.
   - Rule 4 (merge on re-add) is a judgment call that would likely be implemented *differently* in the UI cart logic versus the backend persistence logic, since nothing forces them to agree.
   - Rules 6-8 (submit/pay/cancel transitions) are a classic status check duplicated wherever anything mutates order status: a payment webhook handler, an admin cancel button, a nightly job auto-cancelling abandoned carts.

   "How many places" isn't a fixed number — it's every current and future service that can read-modify-write an order row, which only grows over time. A rule correctly enforced in 5 of 6 call sites today fails the moment a 7th is added by someone who didn't know the other five existed.

2. **What I'd gain:** thread-safety for free — no synchronization needed to read an `Order` concurrently, since it can never change underneath a reader. No "spooky action at a distance" the way Day 16's `Basket` bug worked — an `Order`'s state is exactly what it was when you got the reference, never mutated by something else further down a call chain. It also pairs naturally with the Stretch goal's domain events: if every operation returns a new instance, keeping the full history of an order's versions is close to free.

   **What gets harder:** persistence becomes awkward — `save(Order)` has to replace the stored version rather than mutate in place, and every call site has to be rewritten from `order.submit();` to `order = order.submit();`, which is a real behavioral change everywhere, not just an internal detail. Concurrency control doesn't disappear either, it *relocates*: if two threads load the same `Order` and both call `pay()` independently, you get two divergent "next" versions instead of one in-memory race — you still need an optimistic-concurrency check (a version compare-and-swap) at the repository layer to decide which one wins.

3. Looking at the actual implementation: `OrderLine` is a value object, not an entity — and the deciding factor isn't how it's stored, it's whether the domain needs to track a *specific line's identity* independently of its data.

   `Order.addLine()`'s merge rule is the evidence: re-adding the same SKU doesn't create a second line, it destroys the old `OrderLine` and creates a new one with the combined quantity (`lines.remove(line); lines.add(new OrderLine(...))`). Two lines with identical SKU, quantity, and price are fully interchangeable — nobody can tell, or cares, whether it's "the same line that changed" or "a new line with the same data." That's exactly what a value object is.

   A separate database table and a foreign key to the order don't decide this either way — that's a persistence/ORM detail, and a value object can absolutely live in its own table with a synthetic primary key that nobody in the domain ever looks up or compares by. What *would* flip `OrderLine` to an entity is a real business need to distinguish "same data, different occurrence" — e.g. if two $5 units of the same SKU added at different times needed independent fulfillment status or their own audit trail. Then the line needs a persistent identity separate from its SKU/quantity/price, because merging them would destroy information the business actually cares about. Nothing in this exercise's invariants requires that, so value object is the right call here.

**The trade-off in one line:** Aggregates set your transaction boundary, so a big aggregate means a big lock and a contention hot spot — the same "shard the contended thing" tension from Day 5, one level up at the domain. Design aggregates small; anything that doesn't need transactional consistency belongs in a different one.

**Interview angle:** "I would make `Order` the aggregate root, so status transitions and line edits are enforced in one place and the transaction boundary is one order." That sentence signals you've thought about both consistency and locking — which are the same question wearing different clothes.

**Still fuzzy:**