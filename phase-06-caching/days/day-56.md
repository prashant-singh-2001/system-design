# Day 56 - Consistent hashing

**Phase 6 - Caching** | 45 minutes

## Concept (10 min)

This is the centrepiece of the phase, and the answer to a problem you already felt on Day 49.

**The problem.** `hash(key) % N` distributes beautifully and reshards horribly. Go from 4 nodes
to 5 and roughly 80% of keys change owner, because `h % 4` and `h % 5` disagree for almost every
h. For a cache tier that is a near-total cold start at the exact moment you were adding capacity
*because you were under load*. Systems have died this way - the scale-out that was meant to save
you is what finishes you off.

**The idea.** Map both nodes and keys onto the same circular hash space. A key belongs to the
first node found walking clockwise from the key's position. Add a node and it claims only the arc
between itself and its predecessor - about `1/N` of the keys. Every other key stays exactly where
it was. Remove a node and only its own keys move, to its clockwise neighbour.

**Virtual nodes.** With one point per node the arcs are wildly uneven: random points on a circle
cluster, so one node ends up owning far more than its share. Place each node at many positions
instead - hash `"nodeA#0"`, `"nodeA#1"`, and so on - and the law of large numbers evens it out.
150-200 is the usual range. This also gives you **weighting for free**: a machine with twice the
memory simply gets twice the virtual nodes, which is the same trick as Day 57's weighted round
robin.

This is not a niche technique. It is how Cassandra, DynamoDB, Riak, memcached client libraries
and most CDN request routers decide where data lives.

**The trade-off:** the ring is more state to maintain and agree on than a modulo, and every node
must have the same view of it or keys go missing. In a real cluster that agreement is itself a
distributed systems problem - which is what Phase 8 is about.

## Build (25 min)

Implement `ConsistentHashRing` in `src/main/java/sd/p06/day56/` over a `TreeMap` from hash
position to node name.

The one that catches people is `nodeFor`: use `tailMap(hash)` to find the first position at or
after the key, and **wrap to `firstKey()` when the key falls past the last point**. That wrap is
what makes it a ring; forget it and every key beyond the highest position throws.

The headline test compares key movement against modulo hashing over 10,000 keys. Watch the two
numbers print.

## Reflect (10 min)

1. Write down the two movement percentages you measured. Explain the second one in terms of arcs.
2. Virtual nodes flattened the distribution. What else do they buy you, and what do they cost?
3. Every node must agree on the ring's contents. What happens if two nodes disagree for thirty
   seconds during a deployment?

**Interview angle:** the moment sharding comes up, "modulo hashing means adding a node reshuffles
almost everything, so I would use consistent hashing with virtual nodes - about 1/N of keys move
instead" is one of the highest-value sentences in system design interviews. You now have the
measurement to back it.

## Stretch

Add replication: return the next R distinct nodes clockwise instead of one. You have just built
Dynamo-style replica placement, and you will use exactly this on Day 75 for quorum reads.

## Checkpoint

```powershell
.\day.cmd 56
```
