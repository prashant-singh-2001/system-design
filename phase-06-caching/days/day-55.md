# Day 55 - Distributed invalidation

**Phase 6 - Caching** | 45 minutes

Start Docker Desktop - this one uses a real Redis.

## Concept (10 min)

A local in-process cache is the fastest cache there is: no network, nanoseconds. Then you run
three instances behind a load balancer and you have three copies of every value with no way to
update them together. Instance A handles a write; instances B and C keep serving the old value
until their TTL runs out. The user sees their change, refreshes, and watches it vanish.

The standard remedy is a **broadcast invalidation channel**: whoever changes the data publishes
the key, and every instance drops its local copy. This is what Hibernate's clustered second-level
cache, Caffeine plus a message bus, and Redis client-side caching all do underneath.

Two things to be precise about, because they are where the confidence should stop:

- **This is eventual consistency with a small window.** The message takes a millisecond or two,
  and during that window instance C is still serving stale data. It is much better than a TTL,
  and it is not strong consistency.
- **Pub/sub is fire-and-forget.** An instance that is down, or briefly disconnected, never
  receives the message and stays stale until its TTL rescues it. So **the TTL remains your
  backstop even with invalidation in place.** Anyone claiming broadcast invalidation makes a
  distributed cache strongly consistent has not thought about the partition.

Note also that you broadcast the **key**, never the new value. Day 52's rule holds harder here:
with several publishers there is no ordering between two updates, so broadcasting values lets
instances converge on the older one. Deleting is idempotent and order-independent.

**The trade-off:** you have added a dependency and a failure mode in exchange for a much smaller
staleness window. If your data tolerates a 60-second TTL, do not build this.

## Build (25 min)

In `src/main/java/sd/p06/day55/CacheNode.java` - the Lettuce pub/sub wiring is given, so you are
writing the two methods that matter:

1. `invalidate(key)` - evict locally, then publish `nodeId + "|" + key`.
2. `onInvalidationMessage(message)` - parse, **skip messages this node published itself**, evict,
   and count.

That self-skip matters beyond efficiency: processing your own broadcast is how echo loops start
the moment somebody adds a "re-publish on receive" feature.

## Reflect (10 min)

1. Instance C is restarting when the invalidation is published. How long does it serve stale
   data, and what limits that?
2. Why broadcast the key rather than the new value? Construct the two-writer interleaving that
   makes value-broadcast wrong.
3. What would you need to add to make this strongly consistent - and why is nobody doing that?

**Interview angle:** "local caches with pub/sub invalidation, and a TTL as the backstop for
missed messages" is a complete answer. The TTL clause is what shows you know pub/sub can drop
messages.

## Stretch

Add a version number to each cached value and include it in the broadcast, so a node ignores an
invalidation older than what it already holds. You have just built a Lamport clock, which Day 75
will make formal.

## Checkpoint

```powershell
.\day.cmd 55
```
