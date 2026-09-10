# Day 39 - Build the storage primitive you have been assuming

**Phase 4 - Low-level design craft** | 45 minutes

## Concept (10 min)

Every day since Phase 2 has quietly assumed a key-value store exists somewhere underneath it -
Day 13's `KeyValueStore`, every cache, every rate limiter's internal state. None of them needed
TTL. Today builds the piece that actually does: an in-memory store where entries expire, and
does it the way real caches (Redis included) actually do it, with BOTH of the two mechanisms
production systems rely on together, not just one:

**Lazy expiry, on read.** `get(key)` checks whether the entry's expiry has passed; if so, it
removes the entry and reports it as absent, as if it had never been there. Cheap, and only fires
for keys someone actually asks about again.

**Active sweeping.** `sweepExpired()` scans and removes every currently-expired entry, whether or
not anyone has read it recently. This is what reclaims memory for keys nobody happens to touch
again - lazy expiry alone would leave them sitting in the map forever.

**Capacity eviction adds a third mechanism**, and it is where today's design gets interesting:
when a NEW key needs to be added at capacity, evict whichever LIVE entry has the SOONEST expiry
- it was already closest to being worthless, so it is the cheapest thing to sacrifice. A
permanent entry (no TTL) only gets evicted once no TTL'd entry remains, which is exactly the kind
of secondary tie-break real caches (LRU as a fallback under TTL-based primary eviction, or vice
versa) actually combine in practice.

## Build (25 min)

Implement `ExpiringStore` in `src/main/java/sd/p04/day39/`: `put` (with a TTL), `putPermanent`,
`get` (lazy expiry), `remove`, `size` (sweep first, then count), `sweepExpired`, and
capacity-triggered eviction of the soonest-to-expire live entry.

## Reflect (10 min)

1. `size()` sweeps before counting, which means it is O(n) in the worst case rather than O(1).
   What would a PRODUCTION cache do differently here, and what would it give up to get an O(1)
   `size()`?
2. `evictsSoonestToExpireEntry` picks the entry closest to expiring, not the least-recently-used
   one (Day 32's cache). Describe a workload where TTL-based eviction and LRU eviction would make
   OPPOSITE choices about which entry to sacrifice - and say which one you would actually want
   for that workload.
3. Permanent entries are evicted last, as a fallback with an intentionally unspecified tie-break
   among themselves. Is leaving that unspecified an acceptable design choice, or a gap you would
   close before shipping this? Defend whichever answer you pick.

**Interview angle:** "add a TTL field and check it on read" answers half the question. The
interviewer is listening for whether you volunteer the ACTIVE sweep unprompted - because lazy
expiry alone silently leaks memory for exactly the entries nobody is asking about, which is
precisely the failure mode that would not show up in a quick demo and would show up in
production a week later.

## Stretch

Add a background sweep: a method that runs `sweepExpired()` on a fixed interval using a
`ScheduledExecutorService`, started in the constructor and stopped by a `close()` method. Now
your store proactively reclaims memory without waiting for anyone to call `size()` or
`sweepExpired()` by hand - the shape Redis's own active-expiration cycle actually takes.

## Checkpoint

```powershell
.\day.cmd 39
```
