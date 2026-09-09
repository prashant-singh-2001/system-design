# LLD self-scoring rubric

Use this at the end of a timed round - Day 40, and every LLD practice round after it. Score
yourself honestly; nobody else is going to see this unless you show them.

For each section, score **0** (skipped or badly wrong), **1** (present but thin, or a real gap
in it), or **2** (solid - you would say this out loud in an interview without flinching).

| # | Section | 0 | 1 | 2 | Score |
|---|---|---|---|---|---|
| 1 | **Requirements** - functional list is numbered and complete enough to design against; at least one thing is explicitly scoped OUT | | | | |
| 2 | **Core entities** - the right nouns, each with a stated responsibility and at least one invariant | | | | |
| 3 | **Public API** - method signatures alone tell a reader what the system does; no obviously missing operation | | | | |
| 4 | **Class design** - relationships are clear; you can name which pattern(s) you reached for and why, or say "none needed" and mean it | | | | |
| 5 | **Concurrency** - you answered this even if the answer is "single-threaded, here is why that is fine today" | | | | |
| 6 | **Tests** - the three tests you named would actually catch the bugs you were most worried about, not just the happy path | | | | |
| 7 | **Trade-offs and extensions** - you can name what you deliberately did not build, and what breaks first if a stated new requirement arrived | | | | |

**Total: \_\_\_ / 14**

## Timing

Write your actual elapsed time per section against the template's budget. Running over on
requirements is the single most common way a real 45-minute round fails - if you notice it
happening, that is data, not a failure.

| Section | Budget | Your time |
|---|---|---|
| Requirements | 5 min | |
| Core entities | 5 min | |
| Public API | 10 min | |
| Class design | 10 min | |
| Concurrency | 5 min | |
| Tests | 5 min | |
| Trade-offs | 5 min | |

## Reading your score

- **12-14**: Solid. This is interview-ready. Note which section felt easiest and notice why.
- **8-11**: A real design, with one or two thin spots. Reread whichever section scored a 0 or 1
  and ask yourself, specifically, what a 2 there would have contained.
- **Below 8, or you ran drastically over time**: That is completely normal for an unfamiliar
  problem under a clock the first few times. The fix is never "think harder next time" - it is
  picking ONE section that went worst and drilling exactly that section, alone, on the next
  unfamiliar problem you try.

## The one question worth asking every time

Which section did you rush because you were behind on time, and which section did you rush
because you did not actually know what belongs there? Those are two different problems with two
different fixes - the first is pacing practice, the second is a gap in the method itself.
