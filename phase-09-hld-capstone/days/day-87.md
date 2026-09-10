# Day 87 - Design: ride-hailing

**Phase 9 - HLD and capstone** | 45 minutes

## Concept (10 min)

Ride-hailing is the design where **the query itself is the problem**.

The naive version is `SELECT * FROM drivers WHERE distance(location, me) < 3km`, and it is
unindexable: the predicate depends on the query point, so the database computes a distance for
every driver. At a million drivers that is a full scan, several times a second, per rider.

**Geohash** fixes it by interleaving the bits of latitude and longitude, so nearby points share a
prefix. "Near me" becomes a **prefix match** - which any B-tree, key-value store or Redis sorted
set answers instantly. Precision is a length: 5 characters is about 5 km, 6 about 1 km.

And the flaw you must know, because it is the standard follow-up: **two points metres apart can
have completely different geohashes** if they straddle a cell boundary. A prefix search alone
silently misses the nearest driver. The fix is to search the cell **and its eight neighbours**,
then filter by true distance. Every production geospatial search does this, and today's kernel
makes you feel why.

The second thing that makes this design distinctive is the **write load**. Drivers report location
every few seconds. A million active drivers at one update every 4 seconds is 250,000 writes per
second of data that is worthless 10 seconds later. That is a Phase 5 conclusion arriving in a new
form: this data does not belong in your durable, indexed, replicated database. It belongs in
memory, with a TTL - Redis, sharded geographically.

Then the deep dive worth choosing: **matching**. Assigning the nearest driver greedily is not
optimal globally, and it creates its own problem - two riders matched to the same driver. That is a
distributed lock with a fencing token (Day 77), and this is exactly the case it was built for.

## Build (25 min)

**First (about 10 min)** implement `Geohash` in `src/main/java/sd/p09/day87/`: encoding, haversine
distance, and prefix comparison. The final test shows the two-step prefix-then-filter search.

**Then (about 15 min)** write `designs/ride-hailing.md`. Assume 1M active drivers, location updates
every 4 seconds, and 100k concurrent ride requests at peak.

## Reflect (10 min)

1. 250,000 location writes per second. Where do they go, and what do you deliberately *not* do
   with them?
2. Two riders are matched to the same driver at the same instant. Trace the fix, naming the
   mechanism from Day 77.
3. Surge pricing needs supply and demand per area, updated continuously. Which Phase 7 tool does
   that, and what window?

**Interview angle:** "geohash the driver locations into Redis with a TTL, search the cell plus its
eight neighbours, then filter by true haversine distance" is precise and complete. The neighbours
clause is the detail that shows you have implemented it rather than read about it.

## Stretch

Compare geohash with **H3** (Uber's own hexagonal grid). Hexagons have uniform neighbour distances,
which squares do not - a genuine advantage for exactly this problem, and a good example of a
company building its own primitive because the standard one did not quite fit.

## Checkpoint

```powershell
.\day.cmd 87
```
