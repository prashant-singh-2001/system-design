# Progress

Tick a box when the day's test is green **and** you have written your reflection.
Commit at the end of each day - the log becomes a record of what you understood, and when.

Streak rule: missing a day is fine, missing two in a row is the thing to avoid.


## Phase 1  (days 1-10)

- [ ] **Day 1** - Feel the memory hierarchy. Why sequential access beats random by 10-100x.
- [ ] **Day 2** - Turn a product description into QPS, storage and bandwidth numbers.
- [ ] **Day 3** - Little's Law. Why p99 explodes as utilisation approaches 1.
- [ ] **Day 4** - The JVM memory model: lost updates, visibility, happens-before.
- [ ] **Day 5** - Four ways to count safely, and what each costs under contention.
- [ ] **Day 6** - Virtual threads vs platform threads for blocking work.
- [ ] **Day 7** - Blocking vs non-blocking I/O, and what a Selector actually buys.
- [ ] **Day 8** - HTTP over TCP; what opening a connection really costs.
- [ ] **Day 9** - Wire formats: size, speed, schema evolution.
- [ ] **Day 10** - **Phase review.** A full capacity estimate, defended.

## Phase 2  (days 11-20)

- [ ] **Day 11** - **SRP** - one reason to change. Learn to spot a god class.
- [ ] **Day 12** - **OCP** - extend without editing.
- [ ] **Day 13** - **LSP** - subtypes must honour supertype contracts.
- [ ] **Day 14** - **ISP** - no client should depend on methods it ignores.
- [ ] **Day 15** - **DIP** - depend on abstractions; own your boundaries.
- [ ] **Day 16** - Composition over inheritance; value objects; immutability.
- [ ] **Day 17** - Hexagonal architecture - ports and adapters.
- [ ] **Day 18** - Domain modeling: aggregates, invariants, bounded contexts.
- [ ] **Day 19** - Errors as values vs exceptions; designing failure into an API.
- [ ] **Day 20** - **Phase review.** Refactor under characterization tests.

## Phase 3  (days 21-30)

- [ ] **Day 21** - Strategy + Factory: selecting behaviour at runtime.
- [ ] **Day 22** - Builder + Prototype: complex config without telescoping constructors.
- [ ] **Day 23** - Observer - the seed of pub/sub.
- [ ] **Day 24** - Decorator + Proxy - middleware, caching, lazy loading.
- [ ] **Day 25** - Adapter + Facade - anti-corruption layers.
- [ ] **Day 26** - Command + Chain of Responsibility - request pipelines.
- [ ] **Day 27** - State + Template Method - workflows and lifecycles.
- [ ] **Day 28** - Singleton done right; object pools; flyweight.
- [ ] **Day 29** - Producer-consumer, bounded buffers, poison pills.
- [ ] **Day 30** - **Phase review.** Compose the patterns.

## Phase 4  (days 31-40)

- [ ] **Day 31** - A repeatable LLD method that survives time pressure.
- [ ] **Day 32** - A thread-safe LRU cache: the classic.
- [ ] **Day 33** - Rate limiting: token bucket and sliding window.
- [ ] **Day 34** - Modelling physical resources and allocation.
- [ ] **Day 35** - Scheduling and dispatch under constraints.
- [ ] **Day 36** - Explicit state machines beat scattered boolean flags.
- [ ] **Day 37** - Modelling money, debt and settlement.
- [ ] **Day 38** - Extensible multi-channel dispatch with retries.
- [ ] **Day 39** - Build the storage primitive you have been assuming.
- [ ] **Day 40** - **Phase review.** Perform under the clock.

## Phase 5  (days 41-50)

- [ ] **Day 41** - Get the stack up; schema design and normal forms.
- [ ] **Day 42** - Indexes: B-tree mechanics, `EXPLAIN ANALYZE`, covering indexes.
- [ ] **Day 43** - Isolation levels - reproduce the anomalies, do not recite them.
- [ ] **Day 44** - Locking, deadlocks, MVCC, and why Postgres readers do not block.
- [ ] **Day 45** - Connection pooling and the N+1 problem.
- [ ] **Day 46** - SQL vs NoSQL: model for the access pattern, not the entity.
- [ ] **Day 47** - B-tree vs LSM: the write-optimised trade.
- [ ] **Day 48** - Durability: write-ahead logging and crash recovery.
- [ ] **Day 49** - Sharding: range vs hash, hot keys, the pain of resharding.
- [ ] **Day 50** - **Phase review.** Measure what you built.

## Phase 6  (days 51-60)

- [ ] **Day 51** - Redis up; picking the right data type for the job.
- [ ] **Day 52** - Cache-aside, write-through, write-behind, read-through.
- [ ] **Day 53** - Eviction policies, TTL strategy, measuring hit ratio.
- [ ] **Day 54** - Cache stampede, and its three real fixes.
- [ ] **Day 55** - Distributed invalidation and the cost of coherence.
- [ ] **Day 56** - Consistent hashing: why modulo hashing cannot scale.
- [ ] **Day 57** - Load balancing algorithms, with real nginx in front.
- [ ] **Day 58** - Sticky sessions vs genuinely stateless services.
- [ ] **Day 59** - CDN, edge caching, and `Cache-Control` semantics.
- [ ] **Day 60** - **Phase review.** Prove the win.

## Phase 7  (days 61-70)

- [ ] **Day 61** - Kafka up: topics, partitions, offsets, consumer position.
- [ ] **Day 62** - Producer semantics: `acks`, idempotent producer, batching.
- [ ] **Day 63** - Consumer groups, rebalancing, at-least-once vs exactly-once.
- [ ] **Day 64** - Ordering guarantees and partition key choice.
- [ ] **Day 65** - The outbox pattern: atomic database write plus publish.
- [ ] **Day 66** - Backpressure and flow control.
- [ ] **Day 67** - Dead-letter queues, poison messages, retry topics.
- [ ] **Day 68** - Event-driven architecture, CQRS, event sourcing.
- [ ] **Day 69** - Stream processing: windowing and aggregation.
- [ ] **Day 70** - **Phase review.** End to end.

## Phase 8  (days 71-80)

- [ ] **Day 71** - CAP and PACELC; the consistency model spectrum.
- [ ] **Day 72** - Timeouts, retries, and exponential backoff *with jitter*.
- [ ] **Day 73** - Circuit breaker and bulkhead.
- [ ] **Day 74** - Idempotency keys, dedup, and why exactly-once is a lie.
- [ ] **Day 75** - Replication: leader-follower, multi-leader, leaderless quorum.
- [ ] **Day 76** - Consensus: Raft leader election, implemented.
- [ ] **Day 77** - Distributed locks, Redlock caveats, fencing tokens.
- [ ] **Day 78** - Observability: metrics, structured logs, distributed traces.
- [ ] **Day 79** - SLI, SLO, error budgets; chaos testing.
- [ ] **Day 80** - **Phase review.** Harden it.

## Phase 9  (days 81-90)

- [ ] **Day 81** - The 45-minute HLD framework, and how to control the clock.
- [ ] **Day 82** - Design a URL shortener.
- [ ] **Day 83** - Design a distributed rate limiter service.
- [ ] **Day 84** - Design a news feed.
- [ ] **Day 85** - Design a chat system.
- [ ] **Day 86** - Design video streaming and blob storage.
- [ ] **Day 87** - Design ride-hailing with geospatial indexing.
- [ ] **Day 88** - **Capstone 1.** Architecture and skeleton.
- [ ] **Day 89** - **Capstone 2.** Wire in the machinery.
- [ ] **Day 90** - **Capstone 3.** Prove it, then reflect.
