# Day 69 - Windowing and event time

**Phase 7 - Messaging** | 45 minutes

No Docker today - this is pure Java.

## Concept (10 min)

A batch job has an end; a stream does not. "The average over all of it" is not a question you can
ask about something infinite, so you ask over **windows** instead. Windowing is what makes an
unbounded stream tractable, and every stream processor is built around it.

**Tumbling windows** are fixed and non-overlapping - every event belongs to exactly one. "Orders
per minute."

**Hopping (sliding) windows** overlap: a 5-minute window advancing every minute means each event
lands in five of them. "The 5-minute moving average, updated every minute." Smoother output, more
computation, more state.

Then the idea that makes stream processing genuinely hard: **event time versus processing time.**

An event's event time is when it *happened*; processing time is when you *saw* it. They differ
because phones go through tunnels, networks retry, and consumers lag. Window by processing time
and your minute buckets are wrong whenever anything is delayed - and they are **silently** wrong,
which is worse than an error.

Window by event time and the buckets are correct, but you inherit a question with no clean answer:
**when is a window finished?** An event for 12:00 may arrive at 12:07. Wait forever and you never
emit; close immediately and you drop late data. Real systems use **watermarks** - a heuristic "we
believe we have seen everything up to T" - plus an explicit lateness policy.

There is no correct answer here, only a stated trade between **latency and completeness**. Being
able to say that plainly is the mark of someone who has actually run a streaming pipeline.

## Build (25 min)

In `src/main/java/sd/p07/day69/WindowedAggregator.java`:

1. `windowStart` - floor an instant to a boundary using
   `epochMilli - floorMod(epochMilli, sizeMillis)`. `floorMod` keeps it correct before the epoch.
2. `tumblingSum` - sum per (window, key), windows in ascending order. Use a `LinkedHashMap` so
   output order is deterministic and testable.
3. `hoppingSum` - the same, but each event belongs to every window containing it.
4. `isLate` - true when the event time is before `watermark - allowedLateness`. That one
   comparison is the entire late-data policy.

## Reflect (10 min)

1. The hopping-window total exceeded the stream total. Explain why that is correct rather than a
   bug.
2. Your `allowedLateness` is 5 minutes. A mobile client was offline for an hour. What happens to
   its events, and what would you do about it?
3. Your dashboard shows "orders in the last minute". Which time do you window by, and what
   specifically goes wrong with the other choice?

**Interview angle:** "we window by event time with a watermark and a bounded lateness allowance,
because processing-time windows are silently wrong whenever anything is delayed" is the sentence.
The word *silently* is what makes it land.

## Stretch

Add **session windows**: group events separated by less than a gap duration, so the window length
is determined by the data rather than the clock. That is how you compute "user sessions", and it
is the one window type that cannot be expressed with fixed boundaries.

## Checkpoint

```powershell
.\day.cmd 69
```
