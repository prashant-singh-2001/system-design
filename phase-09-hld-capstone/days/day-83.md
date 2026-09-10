# Day 83 - Design: distributed rate limiter

**Phase 9 - HLD and capstone** | 45 minutes

## Concept (10 min)

You built the algorithms on Day 33. The design question is different and harder: **how do N app
servers share one limit?**

A per-instance limiter across 50 servers allows 50 times your intended rate. The options:

- **Shared store (Redis).** Exact, and costs a network round trip on every request - which you now
  know from Day 8 is a real number, and which you have just added to your *hot* path.
- **Per-instance at `limit/N`.** Free, and wrong whenever traffic is unevenly balanced or an
  instance is down. With sticky sessions or a hash-based balancer it is very wrong.
- **Approximate, with periodic sync.** Instances hold local budget and reconcile every second.
  Bounded overshoot, no per-request round trip. This is what most large systems actually do.

The algorithm choice matters too, and today's kernel shows why. A **fixed window** counter has a
boundary flaw that doubles your limit: 100 requests at 11:59:59 and 100 more at 12:00:01 is two
hundred requests in two seconds, both windows technically satisfied. If the limiter exists to
protect a downstream service, it just failed at exactly the moment it mattered.

A **sliding-window log** is exact and costs memory proportional to the limit. A **sliding-window
counter** interpolates between two fixed windows: approximate, constant memory, and the usual
production compromise.

Two design details that separate a good answer from a complete one:

- **Return `Retry-After`.** A bare 429 makes clients guess, and guessing clients retry in unison -
  Day 72's storm, caused by your own error response.
- **Decide where it runs.** At the edge (API gateway) it protects everything and cannot see
  per-user context. In the service it sees everything and only protects that service. Usually both,
  at different granularities.

## Build (25 min)

**First (about 10 min)** implement `DistributedRateLimiter` in `src/main/java/sd/p09/day83/` - a
sliding-window log with a precise `Retry-After`.

**Then (about 15 min)** write `designs/rate-limiter.md`. Assume a public API at 500k requests/second
across 200 instances, with per-user, per-IP and per-endpoint limits.

## Reflect (10 min)

1. Redis adds a round trip to every request. At what QPS does that stop being acceptable, and what
   would you do instead?
2. Your Redis goes down. Does the API fail open or fail closed? Argue both, then pick.
3. Rate limits are usually per-user. What do you do about unauthenticated traffic, where the only
   identifier is an IP that may be a whole office?

**Interview angle:** "sliding-window counter in Redis, with local per-instance budgets synced
periodically so the hot path avoids a round trip" is a strong answer because it names the cost it
is avoiding. Adding "and we return `Retry-After` so clients back off precisely" shows you have
watched clients misbehave.

## Stretch

Design the fail-open/fail-closed policy properly: rate limiting protecting a fragile downstream
should fail closed; rate limiting for fairness should fail open. Write both into your document
with the reasoning, because whoever is on call will need it.

## Checkpoint

```powershell
.\day.cmd 83
```
