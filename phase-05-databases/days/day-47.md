# Day 47 - B-tree vs LSM: build a storage engine

**Phase 5 - Databases** | 45 minutes

## Concept (10 min)

Today is the day Phase 1 pays off completely.

Day 1 measured one fact: sequential access beats random by 10-100x. A **B-tree** partly ignores
that fact - it updates rows in place, so a write is a random write to wherever the page lives.
Excellent for reads (three page hops to any row), and the reason Postgres is fast at exactly the
workloads you have been giving it.

An **LSM tree** takes that fact seriously and reorganises everything around it:

1. Writes go to an in-memory **memtable**. No disk at all.
2. When it fills, it is flushed as one **SSTable** - a sorted, immutable file written
   sequentially, start to finish.
3. Reads check the memtable first, then each SSTable newest to oldest.
4. **Compaction** merges SSTables in the background, keeping the newest value for each key and
   physically dropping deleted ones.

Every disk write is sequential. That is the whole idea, and it is why LSM engines power
Cassandra, RocksDB, LevelDB, ScyllaDB and Kafka's storage layer.

Two details that carry more weight than they look:

- **Deletes are writes.** You cannot remove a key from an immutable file, so you write a
  **tombstone** - a marker that shadows every older value. Space is not reclaimed until
  compaction. A delete-heavy LSM workload can *grow*, which surprises people in production.
- **Newest wins.** Because a key can appear in several SSTables, read order is the correctness
  rule, not an optimisation. Get the order wrong and you resurrect deleted data.

**The trade-off, stated properly:** LSM buys write throughput with **read amplification** (a read
may touch many files - which is what Bloom filters exist to mitigate) and **space
amplification** (obsolete values linger until compaction). B-trees buy read simplicity with
random writes. Neither is better; they are tuned for opposite ratios, and the read:write ratio
from Day 2 is what picks between them.

## Build (25 min)

In `src/main/java/sd/p05/day47/`:

1. **`MemTable`** - `put` and `get`. The write-optimised half; everything recent lives here.
2. **`LsmStore`** - the coordinator:
   - `put` - write to the memtable, flush if it has reached `flushThreshold`
   - `delete` - `put(key, SSTable.TOMBSTONE)`
   - `get` - memtable first, then each SSTable **newest to oldest**
   - `compact` - merge oldest-to-newest, drop tombstones, write one new SSTable

The tests walk the whole lifecycle: a value in the memtable, a flush, a read falling through to
an SSTable, newest-write-wins across a flush boundary, a tombstone shadowing an older value, and
compaction preserving every live key.

## Reflect (10 min)

1. Why must `get` search SSTables newest-first? Describe the exact bug if you searched
   oldest-first.
2. A tombstone makes a key absent but takes space. When is that space reclaimed, and what does a
   delete-heavy workload do to disk usage before then?
3. Reads may touch every SSTable. What does a Bloom filter change about that, and what does it
   cost? (It is the standard fix, and it is a probabilistic one.)

**Interview angle:** "write-heavy with a range-scan pattern, so an LSM engine - sequential writes,
at the cost of read amplification that Bloom filters mostly absorb" is a real engineering
position. It names the cost, which is what separates it from naming a database.

## Stretch

Add a Bloom filter per SSTable and skip files that certainly do not contain the key. Measure the
drop in files touched per read. You will have built the actual optimisation that makes LSM reads
viable.

## Checkpoint

```powershell
.\day.cmd 47
```
