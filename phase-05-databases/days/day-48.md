# Day 48 - Write-ahead logging and durability

**Phase 5 - Databases** | 45 minutes

## Concept (10 min)

The **D** in ACID. When a write returns successfully, the data must survive the machine losing
power in the next microsecond. Yesterday's LSM store held everything recent in memory - so a
crash lost it. Today you fix that.

The mechanism is a **write-ahead log**, and its rule is one line:

> Append the change to a durable log **before** applying it in memory.

Recovery then replays the log. Anything acknowledged is in the log; anything in the log gets
reapplied. This is how Postgres, MySQL, Kafka, etcd and essentially every durable system works,
and it works because appending to a log is *sequential* - Day 1 again.

Two things people get wrong:

**`write()` does not mean durable.** A successful `write()` puts bytes in the operating system's
page cache, where they sit until the kernel decides to flush. Pull the plug and they are gone.
Durability requires an explicit `fsync`, and `fsync` is expensive - milliseconds, because it
waits for physical media. That single call is the entire cost of durability, and it is why
databases offer knobs to weaken it (`synchronous_commit = off` and its equivalents). Those knobs
trade data loss for throughput. Knowing they exist, and what they cost, is the point.

**A crash can happen mid-write.** So the last record in the log may be *torn* - half-written,
truncated, garbage. Recovery must detect that and stop cleanly at the last complete record
rather than throwing or, far worse, reading nonsense as data. That is what length-prefixing is
for: read the length, check that many bytes are actually there, and if they are not, you have
found the end.

**The trade-off:** every mutation now costs an fsync, so writes are bounded by disk flush latency
rather than memory. The standard mitigation is **group commit** - batch several transactions into
one fsync - which trades a little latency for a lot of throughput. Batching beats chatter, at
every layer.

## Build (25 min)

In `src/main/java/sd/p05/day48/`:

1. **`WriteAheadLog`**
   - `append` - length-prefix the encoded record, append it, **fsync**.
   - `replay` - read length-prefixed records, stopping cleanly at a torn final one.
2. **`DurableKeyValueStore`**
   - `recover` - build a fresh store, replay the WAL, apply each record.
   - `put` - log the PUT, **then** update the map. Order matters; that is the whole principle.
   - `delete` - log the DELETE, then update the map.
   - `get` - read from the in-memory map.

The central test simulates a kill mid-write of the final record by truncating the file, then
asserts recovery yields every complete write and cleanly ignores the torn one.

## Reflect (10 min)

1. Why must the log append happen before the in-memory update? Describe the exact data loss if
   you swapped them.
2. `fsync` costs milliseconds. At 1,000 writes/second, what does that imply - and how does group
   commit rescue it?
3. Recovery replays the whole log. What happens after a year of running, and what mechanism do
   real systems add to bound it? (You are describing checkpoints.)

**Interview angle:** "writes go to a write-ahead log with an fsync before we acknowledge, and we
group-commit to amortise the flush" is a precise durability story. Most answers stop at "we write
to the database", which does not say whether the data survives a power cut.

## Stretch

Add a CRC32 checksum to each record and verify it on replay. Now you detect *corrupted* records,
not just truncated ones - the difference between "the machine died" and "the disk lied".

## Checkpoint

```powershell
.\day.cmd 48
```
