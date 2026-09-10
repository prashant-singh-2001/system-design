# Day 52 - The four caching patterns

**Phase 6 - Caching** | 45 minutes

## Concept (10 min)

Four strategies, one interface, completely different behaviour under failure.

**Cache-aside (lazy loading)** - the one you will use most. Read the cache; on a miss, read the
database and populate. Write the database, then **invalidate** the entry.

Why invalidate rather than update? Because with concurrent writers there is no ordering
guarantee between two clients racing to set the value, so updating can leave the *older* write
winning. Deleting is idempotent and order-independent; the worst outcome is an extra miss.
**"Invalidate, do not update" is the single most useful rule in caching.**

**Read-through** - identical outcome on reads, different *ownership*. The caller only talks to
the cache, and the cache knows how to load its own misses. The difference is not performance, it
is where the logic lives: cache-aside puts it in every caller, read-through puts it in one place.
That matters when six services share a cache and you want one definition of "how do we load a
user".

**Write-through** - write to the database and the cache, synchronously. You get read-your-writes
for free and the cache is never stale. You pay on every write, including for data nobody will
ever read. Order matters: database first, so a failure leaves the cache merely stale rather than
confidently wrong.

**Write-behind (write-back)** - writes land in the cache and return; the database is updated
later, in a batch. The fastest possible write and the most dangerous: **an unflushed write is
lost if the process dies.** It is exactly what your CPU does with its write-back cache, and
exactly what an LSM memtable does (Day 47) - absorb in fast memory, persist in batches, because
batching beats chatter.

**The trade-off, in one line each:** cache-aside pays on misses; write-through pays on writes;
write-behind pays in durability.

## Build (25 min)

Implement the four repositories in `src/main/java/sd/p06/day52/`. `SlowDatabase` counts every
call, so the tests assert on round trips rather than on timing.

Keep the write-behind pending map insertion-ordered so a flush is deterministic.

## Reflect (10 min)

1. Cache-aside invalidates on write. Walk through two concurrent writers to the same key under
   the "update the cache" alternative, and show how the cache ends up holding the older value.
2. Write-behind coalesced 100 updates into one database write. Name a workload where that is
   exactly right, and one where it would be negligent.
3. Write-through writes the database first. What is the state of the world if the *cache* write
   then fails? Is that better or worse than the other order?

**Interview angle:** "cache-aside with invalidation on write" should be your default answer, and
the follow-up worth volunteering is *why* invalidation beats update. Most candidates say the
first half.

## Stretch

Give write-behind a bounded pending map that force-flushes when full. You have now bounded your
data-loss window explicitly - and that number, not a vague "we flush periodically", is what an
SRE will ask you for.

## Checkpoint

```powershell
.\day.cmd 52
```
