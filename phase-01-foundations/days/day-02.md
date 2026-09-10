# Day 2 - Back-of-the-envelope estimation

**Phase 1 - Foundations** | 45 minutes

## Concept (10 min)

Estimation is not about precision. It is about being right on the order of magnitude fast enough
to make a decision, and knowing which single number matters.

The sequence is always the same:

1. **Users** - DAU, plus actions per user per day
2. **QPS** - `DAU x actions / 100,000` (a day is 86,400 seconds; use 100k and say so)
3. **Peak** - 2-3x average, because traffic follows waking hours
4. **Storage** - `writes/day x bytes/write x retention x replication`
5. **Bandwidth** - `QPS x payload`

The output that actually drives design is the **read:write ratio**. Above roughly 10:1 you are
building a read path: cache aggressively, add replicas, denormalise, consider fanout-on-write.
Below 1:1 you are building a write path: batch, partition, reach for LSM-based storage.
Everything else in the estimate is supporting evidence for that one call.

Read `docs/cheatsheets/estimation.md` before you start.

**The trade-off:** rounding aggressively costs you accuracy and buys you speed and confidence.
In a 45-minute design discussion that is overwhelmingly the right bargain - but say your
assumptions out loud, so the error stays visible instead of hiding in the conclusion.

## Build (25 min)

Implement every method in `src/main/java/sd/p01/day02/CapacityEstimator.java`. The
`SystemProfile` record is given. The tests use Twitter-scale numbers - 200M DAU, 2 writes and
100 reads per user per day, 300-byte records, 5 years, 3x replication - and every expected
value is a round number you can verify in your head.

Watch the types: `writesPerUserPerDay` is a `double` but QPS should come back as a `long`.

## Reflect (10 min)

1. The read:write ratio came out at 50:1. Name three concrete design decisions that number
   justifies.
2. Five-year storage came out in the hundreds of terabytes. Does that fit on one machine?
   What does your answer imply about the architecture?
3. You used 100,000 seconds per day instead of 86,400 - a 15% error. Name one estimate in this
   exercise where 15% would actually change a decision, or argue that none would.

**Interview angle:** do the estimate *before* you draw any boxes, and narrate it. "200 million
DAU, 100 reads each, so roughly 200,000 reads per second against 4,000 writes - that is 50 to 1,
so I am going to design the read path first." That single sentence establishes that your
architecture is derived rather than recalled.

## Stretch

Redo the estimate for a photo-sharing product: 100M DAU, 0.5 uploads/day at 2 MB, 50 views/day.
Notice that storage, not QPS, becomes the dominant constraint - and that this changes which
component you would talk about first.

## Approach, if you are stuck

- `writeQps` = `dailyActiveUsers x writesPerUserPerDay / SECONDS_PER_DAY`, cast to `long`
- `readWriteRatio` = reads per day / writes per day (equivalently, read QPS / write QPS)
- `totalStorageBytes` = `storageBytesPerDay x DAYS_PER_YEAR x retentionYears x replicationFactor`
- `peakQps` = `Math.round(averageQps x peakMultiplier)`

## Checkpoint

```powershell
.\day.cmd 2
```
