# Day 89 - Capstone 2: wire in the machinery

**Phase 9 - HLD and capstone** | 45 minutes

Start Docker Desktop.

## Concept (10 min)

Today the service becomes production-shaped, and every piece is a decorator implementing the same
port. Because they share the interface, the domain cannot tell the difference and nothing above
them changes - Day 24's Decorator doing structural work.

- **`CachingLinkRepository`** - cache-aside on the hot path (Day 52). Write through, then
  **invalidate** rather than update, because deleting is idempotent and order-independent while
  updating races with concurrent writers.
- **`ResilientLinkRepository`** - a circuit breaker at the port boundary (Day 73). Reads degrade to
  empty rather than throwing, so a caller gets a 404 in microseconds instead of a thread parked on
  a timeout. Writes fail loudly, because silently discarding a write is worse than failing it.

And then the thing this whole capstone exists to demonstrate. **The wrapping order is a product
decision:**

```
ResilientLinkRepository( CachingLinkRepository( Postgres ) )   -> outage kills every redirect
CachingLinkRepository( ResilientLinkRepository( Postgres ) )   -> cached redirects keep working
```

Put the cache outermost and a total database outage still serves every popular link, because the
cache never asks. Put the breaker outermost and it does not. **That is graceful degradation
decided by one line of wiring**, and it is the single most valuable thing in these three days.

The final test proves it: the database is completely down, and the redirect still works.

## Build (25 min)

In `src/main/java/sd/p09/capstone/adapter/`:

1. `CachingLinkRepository` - cache-aside on `findByCode`; invalidate on `save` and
   `incrementClicks`; delegate `findByTargetUrl` uncached (it is the write path). Do **not**
   accidentally cache a miss as a hit.
2. `ResilientLinkRepository` - consecutive-failure breaker, reads degrade to empty, writes throw,
   one probe after the open period.

## Reflect (10 min)

1. Argue for putting the breaker outermost. There is a real case - name it. Then say why the cache
   outermost is right *here*.
2. Your cache has no TTL and no bound. Name both problems and what you would do about each.
3. A click increments the row and invalidates the cache, so a popular link is evicted constantly.
   What have you just done to your hit ratio, and how would you fix it?

## Design note

Add sections 5 and 6 to `designs/capstone.md`: the architecture diagram, and a deep dive on the
caching-and-resilience wiring - including the ordering argument above, with the failure mode of
each choice spelled out.

## Checkpoint

```powershell
.\day.cmd 89
```
