# Day 32 - A thread-safe LRU cache: the classic

**Phase 4 - Low-level design craft** | 45 minutes

## Concept (10 min)

The LRU cache is the single most-asked LLD problem for a reason: it forces you to combine two
data structures correctly, under a genuine O(1) constraint, and it has an obvious wrong answer
(a `LinkedHashMap` one-liner) that skips the part actually being tested.

The O(1) shape needs a `HashMap<K, Node>` for lookup AND an INTRUSIVE doubly-linked list for
recency order - intrusive meaning the `prev`/`next` pointers live inside the node object itself,
which is what makes moving a node to the most-recently-used end a handful of pointer
reassignments instead of a search. Sentinel `head` and `tail` nodes remove every null-check at
the list's boundaries - a small trick that removes a disproportionate amount of bug surface.

The part that makes today's version different from the version you may have written before:
**`get` is also a write.** Reading an entry moves it in the recency list, which means a
concurrent reader and writer can corrupt the SAME structure a naive implementation only protects
`put` against. Today's cache wraps every operation, `get` included, in one `ReentrantLock` - a
coarse-grained but genuinely correct answer, and a real trade-off worth naming explicitly.

## Build (25 min)

Implement `LruCache` in `src/main/java/sd/p04/day32/`: the intrusive `Node` class is already
sketched (private, nested); build `get` (look up, and if present, splice the node out and
re-insert at the MRU end) and `put` (update-and-move if the key exists; otherwise insert at the
MRU end, evicting the LRU end's node first if at capacity) - all under the given lock.

## Reflect (10 min)

1. A single `ReentrantLock` around every operation means a `get` from one thread blocks a `get`
   from another, even though neither is mutating shared data in a way that would corrupt it if
   they interleaved perfectly. What does Caffeine or Guava's segmented-locking approach buy over
   this, and what does it cost in implementation complexity?
2. `survivesConcurrentHammering` checks that `size()` never exceeds capacity under load, but does
   NOT check that every value returned by `get` is exactly what a single-threaded run would have
   produced. Why is that second property much harder to test meaningfully under real concurrency?
3. Sentinel head/tail nodes hold no real key or value. Walk through, precisely, what extra
   null-checks EVERY list operation would need without them.

**Interview angle:** the interviewer is not testing whether you know `LinkedHashMap` exists -
they are testing whether you can build the intrusive-list mechanic by hand and reason about
where the lock has to go. Reaching immediately for a library type here is the wrong instinct in
an LLD round specifically, even though it would be the right call in production code.

## Stretch

Replace the single lock with a `ReadWriteLock`, and then explain in your notes why that change is
WRONG for this specific class - which method breaks the "read" half of the read-write contract,
and why.

## Checkpoint

```powershell
.\day.cmd 32
```
