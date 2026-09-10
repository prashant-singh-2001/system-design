# Day 21 - Strategy + Factory: selecting behaviour at runtime

**Phase 3 - Patterns** | 45 minutes

## Concept (10 min)

A **Strategy** is an algorithm pulled behind an interface so it can be swapped without touching
whoever calls it - you already built one on Day 12, where each shipping carrier became its own
class behind `ShippingStrategy`. Today's strategy is a `HashFunction`: one interface, several
interchangeable ways of turning a key into a number.

A **Factory** is the piece that turns a runtime CHOICE - a config value, a feature flag, an
environment variable - into the right strategy instance, so that nothing else in the codebase
ever writes `new Fnv1aHashFunction()` directly. Get that centralisation right and swapping the
default hash function for every consumer in the system is a one-line change in one file, not a
grep-and-replace.

You are building this pair for a specific, load-bearing reason: **partitioning**. Sharding a
dataset across N machines means deciding, for every key, which machine owns it - and that
decision is exactly `hash(key) mod N`. Get comfortable with that formula today; you will use it
for real starting Day 49, and you will meet its failure mode - what happens when N changes -
before today is over.

## Build (25 min)

In `src/main/java/sd/p03/day21/`:

1. **`JavaHashFunction`** - widen `String.hashCode()` to a `long`. One line.
2. **`Fnv1aHashFunction`** - implement FNV-1a 32-bit by hand, from the algorithm spelled out in
   the class's javadoc. Two details will silently wreck every value if you miss them: XOR before
   multiply (that is the "a" in FNV-1a), and treating each byte as UNSIGNED (`b & 0xffL`) before
   the XOR.
3. **`HashFunctions`** - the factory: `byName("java")`, `byName("fnv1a")`, or
   `IllegalArgumentException` for anything else.
4. **`Partitioner`** - `Math.floorMod(hashFunction.hash(key), partitionCount)`. Reject
   `partitionCount < 1`.

Check your FNV-1a against the vectors in the javadoc before moving on - `hash("")` must be
exactly `2166136261`.

## Reflect (10 min)

1. `resizingMovesMostKeys` shows the overwhelming majority of keys land in a different partition
   when you go from 4 partitions to 5. Walk through WHY modulo arithmetic has no way to avoid
   this - what would a resharding operation actually have to do today, with this scheme, to add
   one more database?
2. `JavaHashFunction` is free - zero new code - while `Fnv1aHashFunction` took real effort to get
   right. What did you actually buy with that effort? Be specific about what could go wrong
   relying on `String.hashCode()` for something like sharding across JVM versions.
3. The factory rejects unknown names loudly. Sketch the alternative - silently falling back to
   `"java"` for any unrecognised name - and describe the incident report you would eventually
   have to write because of it.

**Interview angle:** "we'd hash the key and mod by the number of shards" is the right first
sentence and an incomplete answer. The strong version immediately continues with "...which
means adding a shard reshuffles almost everything, so real systems use consistent hashing
instead" - naming the problem THIS day exists to make you feel, before Day 56 hands you the fix.

## Stretch

Add a third `HashFunction` - Java's own `MessageDigest.getInstance("SHA-256")`, truncated to the
first 4 bytes - and register it in the factory as `"sha256"`. Compare its distribution against
FNV-1a's on the same 500 keys the test already generates.

## Checkpoint

```powershell
.\day.cmd 21
```
