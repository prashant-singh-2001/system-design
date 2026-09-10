# Day 45 - Connection pools and the N+1 problem

**Phase 5 - Databases** | 45 minutes

## Concept (10 min)

Two of the most common performance bugs in backend work, and both are arithmetic you already
know.

**N+1 queries.** Fetch a list of books, then loop and fetch each book's author. That is 1 query
plus N more. With 100 books over a 0.5 ms datacenter round trip, you have turned one query into
101 and roughly 50 ms of pure network. Across a continent it is 15 seconds. The fix is one JOIN.

The reason this bug is everywhere is that the loop looks completely innocent - it is a `for` loop
over objects, and nothing in the code says "network call". ORMs make it worse by making the
lazy fetch invisible at the call site. The habit worth building is: **count the round trips, not
the lines of code.**

**Connection pool sizing.** Opening a connection is a TCP handshake plus authentication - you
measured the handshake cost on Day 8. So you keep connections open and hand them out. The pool
size then becomes a hard concurrency ceiling, which is Little's Law from Day 3 in a new costume:

```
  max throughput = pool size / query latency
```

A pool of 10 at 50 ms per query cannot exceed 200 queries per second, no matter how many threads
you add. The queue in front of the pool just gets longer, which shows up as latency rather than
errors - the single most confusing failure mode for teams who have not thought about it.

**The trade-off, and it is counter-intuitive:** bigger pools are not better. Each connection is a
backend process on the database with its own memory, and past the point where the database is
saturated you are just adding queueing on the far side, where you cannot see it. The usual
guidance lands near `cores x 2 + spindles` - far smaller than most people's instinct. Measure,
do not guess.

## Build (25 min)

In `src/main/java/sd/p05/day45/`:

1. **`BookRepository`** - the same result as `LegacyBookRepository`, in ONE query instead of
   1 + N. Build the list directly from the JOIN's result set. The test counts actual queries
   through a counting `Connection` wrapper, so you cannot fake it.
2. **`PoolSizingBenchmark`** - run a fixed workload through a real HikariCP pool with
   `concurrentClients` threads, each timing `queriesPerClient` borrowed queries, and measure how
   many actually run at once.

The tests assert that a small pool caps concurrency at its configured size no matter how many
clients contend, and that a pool matching the client count lets nearly everything run at once.

## Reflect (10 min)

1. Your query count went from 1+N to 1. Compute the saving at N=100 for a 0.5 ms round trip, then
   for 40 ms.
2. Your pool is 10 and queries take 50 ms. What is your throughput ceiling? What happens to
   latency at twice that load - and would you see errors, or just slowness?
3. Why is a pool of 200 usually worse than a pool of 20? Where does the queueing move to?

**Interview angle:** "the pool size is a Little's Law calculation, not a guess - at 50 ms per
query a pool of 10 caps us at 200 QPS" connects two things most candidates keep separate, and it
is the kind of connection interviewers are actually listening for.

## Stretch

Add a third repository that uses a single query with `WHERE author_id IN (?, ?, ?)` instead of a
JOIN. Two round trips instead of one, but a simpler query and no row duplication. Work out when
that is the better choice - it genuinely sometimes is.

## Checkpoint

```powershell
.\day.cmd 45
```
