# Back-of-the-envelope estimation

The goal is never precision. It is being **right about the order of magnitude** fast enough to
make a design decision. Round aggressively and say so.

## The sequence

1. **Users** - DAU, and actions per user per day.
2. **QPS** - `DAU x actions / 86,400`. Use 100,000 seconds per day; the error is 15% and nobody cares.
3. **Peak QPS** - 2 to 3x average. State which multiplier you used.
4. **Storage** - `writes/day x bytes/write x retention`, then add replication factor (usually 3x).
5. **Bandwidth** - `QPS x payload size`, split into ingress and egress.
6. **Memory for cache** - the 80/20 rule: 20% of data serves 80% of requests. Size for the hot 20%.

## Round numbers worth knowing

| Thing | Size |
|---|---|
| A UUID | 16 bytes |
| A timestamp | 8 bytes |
| A tweet-sized string | ~200 bytes |
| A typical row with a few columns | ~1 KB |
| A compressed photo | ~200 KB |
| A minute of 1080p video | ~50 MB |

| Power | Value | Name |
|---|---|---|
| 2^10 | ~1,000 | KB |
| 2^20 | ~1 million | MB |
| 2^30 | ~1 billion | GB |
| 2^40 | ~1 trillion | TB |

## A worked example: a Twitter-like feed

- 200M DAU, 2 tweets/day written, 100 tweets/day read
- Write QPS = 200M x 2 / 100k = **4,000/s**; peak **~10,000/s**
- Read QPS = 200M x 100 / 100k = **200,000/s**; peak **~500,000/s**
- Read:write ratio = **50:1** -> this is a read-heavy system, so cache aggressively and
  consider fanout-on-write
- Storage = 400M tweets/day x 300 bytes = 120 GB/day = **~44 TB/year**, x3 replication = **~130 TB/year**

Notice that the ratio, not the absolute numbers, is what drove the design conclusion.
That is what estimation is for.
