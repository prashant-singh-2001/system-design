# Day 40 - Phase review: perform under the clock

**Phase 4 - Low-level design craft** | 45 minutes

## Before you open `JobScheduler.java`

This is the one day this phase where opening the solution file first genuinely costs you
something. Today's system is a **job scheduler**: run a task at (or after) a specific time,
support cancelling a pending job, and don't let one failing job stop the rest of a batch from
running. That is the ENTIRE prompt. Set a timer for 20 minutes and, using
`docs/templates/lld-template.md`, write your own requirements, core entities, public API and
class design for that prompt in `docs/notes/day-40-lld-final.md` - from the prompt alone, not
from the javadoc you have not read yet.

Only once you have done that: open `JobScheduler.java`, compare its actual API to the one you
designed, and implement against it.

## Concept (10 min, spent AFTER your own design pass)

`JobScheduler` reuses two techniques you have now built more than once this phase: an injected
`Clock` so time advances only when a test says so (Days 33 and 39), and per-job failure isolation
so one thrown exception does not stop the rest of a batch (Day 29's worker pool, applied to a
clock-driven sweep instead of a queue). Recognising "I have solved a version of this before" in
the middle of an unfamiliar prompt is a real interview skill, and it is exactly what today is
testing - hence building the scheduler SECOND, after your own blind pass.

## Build (remaining time)

Implement `JobScheduler`: `schedule` (record a pending job, return its id), `cancel` (remove a
pending job, report whether it was actually there), `runDueJobs` (run every due job in `runAt`
order, isolating failures, returning how many were attempted), `pendingCount`.

## Score yourself

Open `docs/templates/lld-rubric.md` and score your OWN design pass - the one you wrote before
touching `JobScheduler.java` - against all seven sections. Be honest about the gap, if any,
between what you planned and what the actual API needed.

1. Which rubric section scored lowest, and was that a TIME problem (you knew what belonged there
   but ran out of clock) or a METHOD problem (you were not sure what belonged there at all)?
2. Compare your own `schedule`/`cancel`/`runDueJobs` API to the given one. Where did they differ,
   and was the difference a genuine design choice or something you simply had not thought of?
3. Look back across all ten days of this phase. Which single idea - the lock placement in Day 32,
   the fit-and-prefer-smallest rule in Day 34, the protocol-vs-business-outcome split in Day 36,
   anything else - do you now reach for without having to think about it? That is the actual
   measure of what this phase built.

## Phase 4 retrospective

Write in `NOTES.md`:

- The **one LLD method habit** from `lld-template.md` you will keep using verbatim
- The **one pattern from Phase 3** that showed up again this phase in a domain you did not expect
- Your rubric score trend if you ran more than one timed round - climbing, flat, or scattered?

Then tick days 31-40 in `PROGRESS.md` and commit.

**Interview angle:** the strongest thing you can do in a real LLD round is narrate the method as
you go - "let me spend a couple of minutes on requirements before I write any code" - because it
tells the interviewer you have a process, not just an answer for this particular prompt. That
narration is worth rehearsing on its own, out loud, independent of any specific system.

## Checkpoint

```powershell
.\day.cmd 40
```
