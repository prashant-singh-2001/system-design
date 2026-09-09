# Day 1 - The memory hierarchy, measured

**Phase 1 - Foundations** | 45 minutes

## Concept (10 min)

Your CPU can execute roughly 3 billion instructions per second. Main memory takes about 100 ns
to answer a request. So a single cache miss costs you around 300 instructions of doing nothing.

The hardware fights this with a hierarchy - L1 (1 ns), L2 (4 ns), L3 (~30 ns), RAM (100 ns) -
and with a prefetcher that watches your access pattern. If you walk memory in order, the
prefetcher loads the next cache line before you ask, and misses nearly vanish. If you jump
around, it cannot predict you, and you pay full price every time.

One cache line is 64 bytes, which is 16 ints. That is the whole trick: sequential access
amortises one memory fetch across 16 elements. Random access does not.

Read `docs/cheatsheets/latency-numbers.md` now. You are about to reproduce two of its rows.

**The trade-off:** locality is free performance, but it constrains your data layout. Arrays are
fast and rigid; linked structures are flexible and cache-hostile. That tension shows up again in
B-trees (Day 47), in row vs column stores (Day 46), and in every "why is this ORM slow" question
you will ever be asked.

## Build (25 min)

Open `src/main/java/sd/p01/day01/LatencyLab.java` and implement:

- `sequentialSum(int[])` - walk the array in order, sum into a `long`, time it with
  `System.nanoTime()`, return a `Measurement`.
- `stridedSum(int[], int stride)` - visit **every element exactly once**, but jumping by
  `stride` so that consecutive accesses land on different cache lines.

The standard shape for the strided version is an outer loop over offsets `0..stride-1` and an
inner loop stepping `i += stride`. Both methods must return the same checksum - the test
enforces that, so you cannot accidentally "win" by doing less work.

## Reflect (10 min)

Write in `NOTES.md`:

1. What ratio did you measure, and what ratio does the cheatsheet predict? If they differ, why?
2. A `LinkedList` of a million integers versus an `int[]` of a million integers - both hold the
   same data. Which is faster to sum, and by roughly how much? Why?
3. Your test used a 64 MB array. Try 1 MB and watch the ratio collapse. What does that tell you
   about where the cliff is on your machine?

**Interview angle:** when someone proposes a design that reads scattered rows in a loop, the
sentence you want is: "that is a random-access pattern - each row is likely a separate page
fetch, so we are paying about N disk seeks rather than one sequential scan." Same principle,
one level down the hierarchy.

## Stretch

Add a third method that accesses elements in genuinely random order (shuffle an index array
first). Compare it with the strided version. Then explain why the strided version is *not*
actually the worst case.

## Approach, if you are stuck

`sequentialSum` is a plain for-loop. For `stridedSum`:

```java
for (int offset = 0; offset < stride; offset++) {
    for (int i = offset; i < data.length; i += stride) {
        sum += data[i];
    }
}
```

Take `System.nanoTime()` before the loops and after, and return
`new Measurement(elapsed, sum)`.

## Checkpoint

```powershell
.\day.cmd 1
```
