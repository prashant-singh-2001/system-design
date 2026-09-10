# Day 51 - Redis: picking the right data type

**Phase 6 - Caching** | 45 minutes

Start Docker Desktop. The test starts and throws away its own Redis via Testcontainers.

## Concept (10 min)

Redis is not "a place to put strings". It is a set of server-side data structures, and choosing
the right one is the difference between one round trip and a thousand.

The rule of thumb: **push the work to where the data already is.** Fetching a list into Java,
sorting it, and taking the top ten is orders of magnitude more expensive than asking Redis for
the top ten - because Redis keeps it sorted on write and the answer never crosses the network.

Five jobs, five obviously correct types:

- **HASH** for objects. A JSON string works until you need to update one field, and then it is
  read-modify-write over the network - which is Day 4's lost update, at a distance.
- **SORTED SET** for leaderboards and anything ranked. Ordered on write, O(log N) reads.
- **HYPERLOGLOG** for approximate cardinality. A SET is exact and grows with the data; a
  million visitors is a million members. HyperLogLog answers in about 12 KB regardless, with
  roughly 0.81% error. For a dashboard that is a superb trade. For billing it is not.
- **STRING with INCR** for counters. `INCR` is atomic and returns the new value, so there is no
  read-modify-write race at all.
- **TTL on everything that is not the source of truth.** A cache without expiry is a memory leak.

**The trade-off running through all of it:** these structures live in RAM, and RAM is expensive
and finite. Every choice here is really "how much memory will I spend for how much speed", and
HyperLogLog is the clearest case - it gives up exactness to make the memory constant.

## Build (25 min)

Implement `RedisDataTypeLab` in `src/main/java/sd/p06/day51/`. Five pairs of methods, each
using the type named in its javadoc.

The one with a real trap is `incrementRequestCount`. Set the TTL **only when the counter is
first created** - that is, when `incr` returns 1. Calling `expire` on every request pushes the
deadline forward continually and the window never resets, so a client sending steady traffic is
never rate limited. That is a genuine production bug and the test checks for it.

## Reflect (10 min)

1. You stored the profile as a HASH. What exactly goes wrong with a JSON string when two
   requests update different fields at the same time?
2. HyperLogLog reported a number close to but not equal to 10,000. Name one metric where that is
   perfect and one where it would be unacceptable.
3. Every key here has a TTL except the leaderboard. Should it? What decides?

**Interview angle:** "we would use a Redis sorted set for the leaderboard, so the top-N read is
O(log N) server-side rather than fetching and sorting in the application" is concrete and shows
you know Redis is more than a hash map.

## Stretch

Add a sliding-window rate limiter using a sorted set keyed by timestamp: `ZADD` the request
time, `ZREMRANGEBYSCORE` to drop everything outside the window, then `ZCARD`. Compare its
accuracy against today's fixed window - you will build both properly on Day 33's algorithms.

## Checkpoint

```powershell
.\day.cmd 51
```
