# Day 42 - Indexes and the query planner

**Phase 5 - Databases** | 45 minutes

## Concept (10 min)

An index is a B-tree, and a B-tree is Day 1's cost model turned into a data structure. Its nodes
are sized to a disk page precisely because a page is the unit you pay for. A million-row table is
about three levels deep, so a lookup is three page reads instead of a million-row scan. That
factor is not a tuning detail - it is the difference between a product working and not.

The three plans you will see today, in order of cost:

- **Seq Scan** - read every row. Correct, and fine on small tables; catastrophic on large ones.
- **Index Scan** - walk the B-tree to find matching rows, then fetch each row from the heap for
  the columns the index does not carry.
- **Index Only Scan** - the index carries every column the query needs, so the heap is never
  touched at all. This is what a *covering* index buys.

`EXPLAIN ANALYZE` is the tool, and the habit worth building is: **never claim a query is fast,
show the plan.** The planner's own output is evidence; an opinion about indexes is not. This is
also why the exercise makes you save both plans to a file - a performance change without a
before-and-after is a story, not a result.

**The trade-off:** every index is a second data structure that must be updated on every write,
and it consumes memory and disk. Indexes make reads fast and writes slower. On a 50:1 read-heavy
system that is obviously right; on a write-heavy ingest table it may be obviously wrong. The
read:write ratio you computed on Day 2 is what decides.

One more thing worth internalising: the planner is a **cost-based optimiser**, and it makes its
choices from table statistics. That is why the exercise runs `ANALYZE` after creating an index.
A perfectly good index gets ignored when the statistics are stale - a genuinely common and
confusing production failure.

## Build (25 min)

In `src/main/java/sd/p05/day42/IndexingLab.java`:

1. Run `EXPLAIN (ANALYZE, FORMAT TEXT)` for a query and concatenate the plan rows into a string.
2. Create a plain index on `events(user_id)`, then `ANALYZE`.
3. Replace it with a covering index that `INCLUDE`s `event_type`, then `VACUUM ANALYZE`.
4. Classify a plan: check for `Index Only Scan` **before** `Index Scan` (the substring `Index
   Scan` also matches inside `Index Only Scan`, so order matters), then `Seq Scan`, else `OTHER`.
5. Write both plans to a small markdown file as evidence.

## Reflect (10 min)

1. Paste your three plans side by side. What did the row-count estimate do, and what did the
   actual time do?
2. The covering index avoided the heap entirely. What did that cost you on writes and on disk?
3. Why does `VACUUM ANALYZE` matter before an Index Only Scan appears? (Look up the visibility
   map - the answer is more interesting than it sounds.)

**Interview angle:** "I would add an index on that column" is the expected answer. "I would check
the plan first - if it is already an Index Scan, the win is a covering index so we skip the heap
fetch entirely" shows you have actually read a plan.

## Stretch

Add a second column to the query's `WHERE` clause and watch a single-column index stop helping.
Then build a composite index and work out why column order in it matters.

## Checkpoint

```powershell
.\day.cmd 42
```
