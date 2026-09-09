# The 90 Days

Nine phases, ten days each, 45 minutes a day. Each day lists its **objective** (what you should
be able to explain afterwards) and its **deliverable** (what must exist in the repo).

Phases build on each other deliberately: you cannot reason about sharding until you understand
what a disk seek costs, and you cannot design a feed until you have built a cache that stampedes.

| Phase | Days | Theme |
|---|---|---|
| 1 | 1-10 | Foundations: the machine and the numbers |
| 2 | 11-20 | Design principles: SOLID and clean architecture |
| 3 | 21-30 | Patterns that actually appear in system design |
| 4 | 31-40 | Low-level design craft |
| 5 | 41-50 | Databases and storage internals -- *Docker starts here* |
| 6 | 51-60 | Caching and load distribution |
| 7 | 61-70 | Async, messaging and streams |
| 8 | 71-80 | Reliability and distributed systems |
| 9 | 81-90 | High-level design and capstone |

---

## Phase 1 - Foundations: the machine and the numbers

Every scalability argument bottoms out in physics: a cache miss costs ~100 ns, a disk seek
~100 us, a cross-continent round trip ~150 ms. You will *measure* these rather than memorise
them, because numbers you produced yourself are the ones you trust under pressure.

| Day | Objective | Deliverable |
|---|---|---|
| 1 | Feel the memory hierarchy. Why sequential access beats random by 10-100x. | `LatencyLab` measuring cache-friendly vs cache-hostile array traversal |
| 2 | Turn a product description into QPS, storage and bandwidth numbers. | `CapacityEstimator`: DAU to peak QPS to 5-year storage |
| 3 | Little's Law. Why p99 explodes as utilisation approaches 1. | `LittlesLaw` plus a queue simulator showing the knee of the curve |
| 4 | The JVM memory model: lost updates, visibility, happens-before. | A racy counter that loses writes, and a correct one that does not |
| 5 | Four ways to count safely, and what each costs under contention. | `synchronized` / `ReentrantLock` / `AtomicLong` / `LongAdder`, benchmarked |
| 6 | Virtual threads vs platform threads for blocking work. | 10,000 blocking tasks on both; explain the wall-clock gap |
| 7 | Blocking vs non-blocking I/O, and what a Selector actually buys. | A thread-per-connection echo server and an NIO Selector one |
| 8 | HTTP over TCP; what opening a connection really costs. | A hand-rolled HTTP server plus a pooled vs unpooled client comparison |
| 9 | Wire formats: size, speed, schema evolution. | JSON vs compact binary vs Java serialization, round-tripped and measured |
| 10 | **Phase review.** A full capacity estimate, defended. | Estimation for a Twitter-scale feed, written up in `docs/` |

## Phase 2 - Design principles: SOLID and clean architecture

SOLID is usually taught as five slogans. Here each principle is a **refactor of deliberately bad
code** under a test suite that must keep passing. You will feel what the principle buys, which is
the only way to learn when to ignore it.

| Day | Objective | Deliverable |
|---|---|---|
| 11 | **SRP** - one reason to change. Learn to spot a god class. | Split `OrderProcessor` so pricing is testable without a database |
| 12 | **OCP** - extend without editing. | Replace a carrier if/else chain with a strategy registry |
| 13 | **LSP** - subtypes must honour supertype contracts. | Fix a broken hierarchy; prove it with one shared contract test |
| 14 | **ISP** - no client should depend on methods it ignores. | Split a 12-method repository so read-only clients stay read-only |
| 15 | **DIP** - depend on abstractions; own your boundaries. | Invert a hard-coded DAO; swap in a fake with no Docker running |
| 16 | Composition over inheritance; value objects; immutability. | A `Money` value object that cannot be corrupted by aliasing |
| 17 | Hexagonal architecture - ports and adapters. | A URL shortener whose domain imports nothing from any adapter |
| 18 | Domain modeling: aggregates, invariants, bounded contexts. | An `Order` aggregate that cannot be constructed in an invalid state |
| 19 | Errors as values vs exceptions; designing failure into an API. | A sealed `Result` type and an API rewritten to use it |
| 20 | **Phase review.** Refactor under characterization tests. | A legacy service made clean without breaking a single test |

## Phase 3 - Patterns that actually appear in system design

Not all 23 Gang of Four patterns. The eight or so that keep reappearing once you start drawing
distributed architectures, because a middleware chain *is* Decorator and pub/sub *is* Observer.

| Day | Objective | Deliverable |
|---|---|---|
| 21 | Strategy + Factory: selecting behaviour at runtime. | A pluggable hashing and partitioning strategy set |
| 22 | Builder + Prototype: complex config without telescoping constructors. | A validated, immutable service-config builder |
| 23 | Observer - the seed of pub/sub. | An in-process event bus with typed subscribers |
| 24 | Decorator + Proxy - middleware, caching, lazy loading. | A decorator stack: timing, then caching, then retry |
| 25 | Adapter + Facade - anti-corruption layers. | An adapter isolating your domain from a hostile third-party API |
| 26 | Command + Chain of Responsibility - request pipelines. | A filter chain that authenticates, rate-limits, then handles |
| 27 | State + Template Method - workflows and lifecycles. | An order lifecycle as an explicit state machine |
| 28 | Singleton done right; object pools; flyweight. | A correct lazy singleton and a bounded resource pool |
| 29 | Producer-consumer, bounded buffers, poison pills. | A shutdown-clean worker pool over a `BlockingQueue` |
| 30 | **Phase review.** Compose the patterns. | A small pluggable request-pipeline framework |

## Phase 4 - Low-level design craft

The interview asks you to design a parking lot in 45 minutes. The job asks you to design a
service in 45 minutes. Same skill: requirements, entities, interfaces, tests - fast, with the
concurrency thought through rather than hand-waved.

| Day | Objective | Deliverable |
|---|---|---|
| 31 | A repeatable LLD method that survives time pressure. | The method in `docs/templates/lld-template.md`, applied once |
| 32 | A thread-safe LRU cache: the classic. | O(1) get/put via hash map plus intrusive linked list, concurrency-safe |
| 33 | Rate limiting: token bucket and sliding window. | Both algorithms, with their burst behaviour pinned by tests |
| 34 | Modelling physical resources and allocation. | Parking lot: spot types, pricing strategy, concurrent allocation |
| 35 | Scheduling and dispatch under constraints. | Elevator system with a pluggable dispatch strategy |
| 36 | Explicit state machines beat scattered boolean flags. | Vending machine where illegal transitions cannot happen |
| 37 | Modelling money, debt and settlement. | Splitwise: equal/exact/percentage splits, simplified settlement |
| 38 | Extensible multi-channel dispatch with retries. | Notification service: email, SMS and push behind one port |
| 39 | Build the storage primitive you have been assuming. | In-memory KV store with TTL, eviction and expiry sweeps |
| 40 | **Phase review.** Perform under the clock. | A timed 45-minute LLD round, self-scored against a rubric |

## Phase 5 - Databases and storage internals

Docker starts here. From now on you talk to real Postgres, real Redis, real Kafka - because
"the database will handle it" stops being an acceptable answer the moment somebody asks
*which isolation level*.

| Day | Objective | Deliverable |
|---|---|---|
| 41 | Get the stack up; schema design and normal forms. | `infra/docker-compose.yml` running; a schema with real constraints |
| 42 | Indexes: B-tree mechanics, `EXPLAIN ANALYZE`, covering indexes. | A slow query made fast, with both plans saved as evidence |
| 43 | Isolation levels - reproduce the anomalies, do not recite them. | Dirty, non-repeatable and phantom reads triggered on two live connections |
| 44 | Locking, deadlocks, MVCC, and why Postgres readers do not block. | A deadlock you cause on purpose, then fix by lock ordering |
| 45 | Connection pooling and the N+1 problem. | A HikariCP sizing experiment; an N+1 found and eliminated |
| 46 | SQL vs NoSQL: model for the access pattern, not the entity. | One feature modelled relationally and wide-column, compared |
| 47 | B-tree vs LSM: the write-optimised trade. | A mini LSM engine: memtable, SSTable flush, compaction |
| 48 | Durability: write-ahead logging and crash recovery. | A WAL that survives a simulated kill mid-write |
| 49 | Sharding: range vs hash, hot keys, the pain of resharding. | A sharded store, a deliberately hot partition, and its fix |
| 50 | **Phase review.** Measure what you built. | Benchmark the sharded store; write up the trade-offs |

## Phase 6 - Caching and load distribution

A cache is the cheapest scalability win and the richest source of production incidents. You will
build one, break it, and then fix the specific ways it breaks at scale.

| Day | Objective | Deliverable |
|---|---|---|
| 51 | Redis up; picking the right data type for the job. | Strings, hashes, sorted sets and HyperLogLog, each used deliberately |
| 52 | Cache-aside, write-through, write-behind, read-through. | All four implemented behind one interface, with consistency notes |
| 53 | Eviction policies, TTL strategy, measuring hit ratio. | An instrumented cache reporting hit ratio and eviction counts |
| 54 | Cache stampede, and its three real fixes. | Reproduce a stampede; fix with locking, early recompute, jittered TTL |
| 55 | Distributed invalidation and the cost of coherence. | Pub/sub invalidation across two app instances |
| 56 | Consistent hashing: why modulo hashing cannot scale. | Implement it; measure key movement when a node joins |
| 57 | Load balancing algorithms, with real nginx in front. | nginx balancing three Java instances; compare RR, least-conn, hash |
| 58 | Sticky sessions vs genuinely stateless services. | Move session state out; prove any instance can serve any request |
| 59 | CDN, edge caching, and `Cache-Control` semantics. | Correct cache headers; explain what each directive changes |
| 60 | **Phase review.** Prove the win. | Add caching to your service; measure p99 before and after |

## Phase 7 - Async, messaging and streams

Synchronous calls couple availability: if you call five services in a row, your uptime is the
product of theirs. Messaging is how you break that chain - and where you meet delivery semantics.

| Day | Objective | Deliverable |
|---|---|---|
| 61 | Kafka up: topics, partitions, offsets, consumer position. | Produce and consume across a multi-partition topic |
| 62 | Producer semantics: `acks`, idempotent producer, batching. | Measure the throughput and durability cost of each `acks` setting |
| 63 | Consumer groups, rebalancing, at-least-once vs exactly-once. | A consumer that survives rebalance without losing or duplicating work |
| 64 | Ordering guarantees and partition key choice. | Demonstrate ordering held within a partition and lost across partitions |
| 65 | The outbox pattern: atomic database write plus publish. | Transactional outbox with a relay, tested against a crash |
| 66 | Backpressure and flow control. | A slow consumer that degrades gracefully instead of falling over |
| 67 | Dead-letter queues, poison messages, retry topics. | A DLQ with bounded retries and replay tooling |
| 68 | Event-driven architecture, CQRS, event sourcing. | A read model rebuilt purely by replaying the event log |
| 69 | Stream processing: windowing and aggregation. | A tumbling-window aggregate over the event stream |
| 70 | **Phase review.** End to end. | An event-driven pipeline: API to outbox to Kafka to projection |

## Phase 8 - Reliability and distributed systems

Everything above assumed the happy path. This phase removes that assumption: networks partition,
clocks drift, nodes die mid-write, and retries turn one failure into a thundering herd.

| Day | Objective | Deliverable |
|---|---|---|
| 71 | CAP and PACELC; the consistency model spectrum. | A decision table mapping requirements to a consistency choice |
| 72 | Timeouts, retries, and exponential backoff *with jitter*. | Simulate a retry storm; kill it with jittered backoff and budgets |
| 73 | Circuit breaker and bulkhead. | Resilience4j breaker; show a failing dependency being isolated |
| 74 | Idempotency keys, dedup, and why exactly-once is a lie. | An idempotent endpoint safe under client retries |
| 75 | Replication: leader-follower, multi-leader, leaderless quorum. | A quorum store; tune R and W and observe the consistency change |
| 76 | Consensus: Raft leader election, implemented. | Leader election over simulated nodes with message loss |
| 77 | Distributed locks, Redlock caveats, fencing tokens. | A lock that stays correct across a GC pause, via fencing |
| 78 | Observability: metrics, structured logs, distributed traces. | Micrometer to Prometheus to Grafana, with a real dashboard |
| 79 | SLI, SLO, error budgets; chaos testing. | Define SLOs, then kill containers and watch the budget burn |
| 80 | **Phase review.** Harden it. | Your service survives a chaos run with SLOs intact |

## Phase 9 - High-level design and capstone

The synthesis. Each design day produces a real document in
`phase-09-hld-capstone/designs/`, written to the structure an interviewer expects: requirements,
estimates, API, data model, high-level diagram, deep dive, bottlenecks.

| Day | Objective | Deliverable |
|---|---|---|
| 81 | The 45-minute HLD framework, and how to control the clock. | The framework in `docs/cheatsheets/hld-framework.md`, rehearsed |
| 82 | Design a URL shortener. | `designs/url-shortener.md`: key generation, redirect path, analytics |
| 83 | Design a distributed rate limiter service. | `designs/rate-limiter.md`: algorithm, placement, sync across nodes |
| 84 | Design a news feed. | `designs/news-feed.md`: fanout-on-write vs read, the celebrity problem |
| 85 | Design a chat system. | `designs/chat.md`: WebSockets, presence, delivery receipts, fanout |
| 86 | Design video streaming and blob storage. | `designs/video-streaming.md`: chunking, transcoding, CDN, adaptive bitrate |
| 87 | Design ride-hailing with geospatial indexing. | `designs/ride-hailing.md`: geohash/quadtree, matching, location updates |
| 88 | **Capstone 1.** Architecture and skeleton. | Design doc plus a running service with its schema |
| 89 | **Capstone 2.** Wire in the machinery. | Cache, queue, replication and resilience integrated |
| 90 | **Capstone 3.** Prove it, then reflect. | Load test, Grafana dashboard, final design doc, program retrospective |

---

## After Day 90

You will have written roughly 8,000 lines of Java, run real Postgres, Redis and Kafka, and
produced nine design documents. The most valuable artefact is the last one: a retrospective
naming the five ideas that changed how you think. Reread it before any design interview.
