# Day 49 - Sharding, and the hot key

**Phase 5 - Databases** | 45 minutes

## Concept (10 min)

One machine has a ceiling. Sharding splits the data across many, and the only real question is:
**which shard owns this key?**

**Hash sharding** - `Math.floorMod(key.hashCode(), shardCount)`. This is Day 21's formula again.
It distributes evenly, and it destroys range queries: consecutive keys land on unrelated shards,
so "all orders from March" becomes a query to every shard.

**Range sharding** - each shard owns a contiguous slice of the key space. Range queries are cheap
and hit one shard. But an ordered key like a timestamp sends *every* write to the last shard,
which is now your bottleneck while the others idle.

Neither is right; they trade the same thing in opposite directions. Even distribution and range
locality are in direct conflict, and you pick based on your access pattern.

Then the problem that dominates real systems: **the hot key.** Distribution assumes roughly
uniform traffic per key. Real traffic is Zipfian - one celebrity, one viral product, one
enormous tenant. A single key is by definition on a single shard, so that shard melts while the
others are idle. Adding shards does not help at all, because the hot key does not split.

The standard fix is **salting**: write the hot key as `key#0` through `key#N`, spreading its
traffic across N shards, and sum the N counters on read. You have traded read complexity for
write distribution - and you have created a new obligation, because the read must now know to
fan in. Notice this is the same move as `LongAdder` on Day 5. Sharding a contended thing is one
idea appearing at three scales.

**The trade-off nobody mentions until it hurts:** resharding. Changing `shardCount` moves almost
every key, because `hash % 4` and `hash % 5` disagree for most inputs. With terabytes live, that
is a migration project, not a config change. Day 56's consistent hashing exists precisely to
make this cheap.

## Build (25 min)

In `src/main/java/sd/p05/day49/`:

1. **`HashShardingStrategy`** - `floorMod` the key's hash by `shardCount`.
2. **`RangeShardingStrategy`** - find the first boundary `>=` the key, or the last shard if none.
3. **`ShardedCounterStore`** - validate `shardCount >= 1`, one map and one counter per shard;
   route through the strategy, bump the shard's counter, and read a key's counter from its shard.

The tests demonstrate a skewed workload concentrating on one shard, then salting flattening it.

## Reflect (10 min)

1. Your workload is "all events for user X in the last hour". Which strategy, and why?
2. Salting spread the hot key. What did the read path have to do in exchange, and what happens if
   a reader forgets?
3. You need to go from 4 shards to 5. Roughly what fraction of keys move? What does that mean
   operationally at a terabyte?

**Interview angle:** get to the hot key before you are asked. "Hash sharding on user_id
distributes evenly, but a celebrity account is a hot key on one shard, so I would salt those and
fan in on read" is the answer that shows you have run a sharded system rather than read about one.

## Stretch

Implement `rebalance(newShardCount)` for the hash strategy and count how many keys move. Then
predict what consistent hashing would do to that number - you will build it on Day 56 and can
check your prediction.

## Checkpoint

```powershell
.\day.cmd 49
```
