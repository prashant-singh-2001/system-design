# Day 33 - Rate limiting: token bucket and sliding window

**Phase 4 - Low-level design craft** | 45 minutes

## Concept (10 min)

Day 26 built a rate limiter that counted requests per IP with no time dimension at all - useful
for learning Chain of Responsibility, useless in production. Today builds the two algorithms
real systems actually use, and - more importantly - pins down exactly how their BURST behaviour
differs, because that difference is the entire reason to choose one over the other.

**Token bucket**: a bucket holds up to `capacity` tokens, refilling continuously at a fixed rate.
Every allowed request consumes one token. Starting full means an idle bucket lets an immediate
burst of up to `capacity` requests through, then throttles to the steady refill rate - smooth,
continuous replenishment, because a fractional second of elapsed time refills a fractional
token.

**Sliding window (log)**: track the timestamp of every recent request; allow a new one only if
fewer than `maxRequests` timestamps remain inside the trailing window. No smooth refill exists
here - capacity only returns as OLD individual timestamps individually age out. Send a burst of
`maxRequests` requests all at once, and none of that capacity comes back until each of those
specific requests, one by one, falls outside the window - which for a tight burst can mean
waiting nearly the entire window duration before anything frees up.

Both need a way to test "time passing" without actually waiting - inject a `Clock` (Day 17's
lesson, again) so a test can advance simulated time instantly and deterministically.

## Build (25 min)

In `src/main/java/sd/p04/day33/`:

- **`TokenBucketRateLimiter`** - refill based on elapsed time since the last refill, capped at
  `capacity`; consume one token if at least one is available.
- **`SlidingWindowRateLimiter`** - evict timestamps older than the trailing window, then admit if
  under `maxRequests`.

## Reflect (10 min)

1. Configure both limiters with the same numbers - `capacity`/`maxRequests` of 10 and an
   equivalent rate. At time zero, do they behave identically? At what point in a bursty traffic
   pattern do they diverge, concretely?
2. `tokenBucketCapsAtCapacity` advances the clock by 1,000 seconds and confirms the bucket does
   NOT hand out 1,000 tokens. What real production incident does the capacity cap prevent, for a
   limiter that briefly had no traffic (an idle service just starting up, say)?
3. The sliding window log remembers every recent timestamp - memory proportional to
   `maxRequests`. A "sliding window COUNTER" (two adjacent fixed windows, weighted by how far
   into the current one you are) approximates the same idea in constant memory. What accuracy do
   you give up for that?

**Interview angle:** "we'd use a token bucket" is an answer with no content until you can say
WHY - specifically, that it allows a bounded burst on top of a steady average rate, which is
usually what you want for a client, and specifically NOT what you want if the requirement is "no
more than N requests in ANY rolling window, ever, no exceptions" - which is exactly when sliding
window is the right call instead.

## Stretch

Implement a third limiter, `SlidingWindowCounterRateLimiter`, using two fixed windows and a
weighted estimate instead of a full timestamp log. Write a test showing where its approximation
disagrees with the exact log-based version, and by how much.

## Checkpoint

```powershell
.\day.cmd 33
```
