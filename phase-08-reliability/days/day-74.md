# Day 74 - Idempotency keys

**Phase 8 - Reliability** | 45 minutes

## Concept (10 min)

Phase 7 kept borrowing this. Here is the actual mechanism.

A client sends "charge this card". The network times out. The client has **no way to tell** whether
the charge happened. Both actions are wrong: retrying may double-charge, not retrying may lose the
payment. The uncertainty is irreducible - no better network fixes it, because the failure could be
in either direction.

So you move the decision to the server. The client generates a unique key per logical operation and
sends it with every attempt. The server does the work **at most once per key** and returns the same
answer to every repeat. Retrying becomes safe, so the client's choice becomes easy.

Three properties that are easy to get wrong:

- **The client generates the key, not the server.** A server-generated key changes on each retry
  and deduplicates nothing.
- **The key covers a logical operation, not a request.** Same key for a retry of the same intent;
  a new key for a genuinely new payment. Charging Alice £50 twice on purpose is two operations.
- **Return the original result on replay**, not just a success. The client needs the same payment
  id it would have received the first time.

And one implementation detail that is a correctness requirement: **the claim must be atomic.** A
`containsKey` followed by a `put` has a window wide enough for two concurrent retries to both see
"absent" and both proceed - reintroducing the exact bug idempotency exists to prevent. Use
`putIfAbsent`, or a unique constraint, or `SET NX`.

Equally: a **failed** attempt must release its claim. Otherwise a transient failure becomes a
permanent one, because the key stays claimed forever.

**The trade-off:** you now operate a store, its TTL bounds how long retries are safe, and a key
reused with a different payload is a genuine hazard - real APIs return 422 for that. In exchange,
every client can retry freely, which is what makes at-least-once delivery workable at all.

## Build (25 min)

In `src/main/java/sd/p08/day74/`:

1. `InMemoryIdempotencyStore` - atomic `claim`, plus result storage and `release`.
2. `PaymentService.charge` - replay a completed result; claim, work, store; release and rethrow on
   failure; refuse with `ConcurrentAttemptException` when another attempt holds the key.

## Reflect (10 min)

1. The concurrency test made 50 attempts and one gateway call. Show the interleaving that would
   produce two calls if `claim` were not atomic.
2. Your store has a 24-hour TTL. A client retries after 25 hours. What happens, and is that right?
3. A client reuses a key with a different amount. What should the API do, and why is silently
   replaying the old result the wrong answer?

**Interview angle:** whenever a design involves money or a retryable write, "the client sends an
idempotency key and we deduplicate server-side, returning the original result on replay" is the
sentence. Adding "the claim has to be atomic" is what shows you have implemented one.

## Stretch

Add payload hashing: store a hash of the request body with the key and return a 422-equivalent
when a key arrives with different content. You have now closed the last real hole in the pattern.

## Checkpoint

```powershell
.\day.cmd 74
```
