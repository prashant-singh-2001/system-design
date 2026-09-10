# Phase 9 - High-level design and capstone

**Days 81-90**

## Why this phase sits here

The synthesis. Each design day produces a real document in `designs/`, written to the structure
an interviewer expects: requirements, estimates, API, data model, architecture, deep dive,
bottlenecks.

By now none of the components are mysterious - you have built a cache, a rate limiter, a sharded
store, a consumer group and a leader election. High-level design stops being vocabulary
recall and becomes what it should be: choosing between things you have used.

The last three days are a capstone: build it, wire it, load-test it, and write the design doc.

## The days

See `CURRICULUM.md` for the full day-by-day list with objectives and deliverables.

## The one idea to carry forward

**A design is a set of defended trade-offs, not a diagram.** The boxes are the easy part; being
able to say what breaks first, and at what number, is the job.

---

## Status: ready

Ten day briefs in `days/`, with starter classes and failing tests under `src/`.

**Days 81-87 are writing days.** Each has a small algorithmic kernel - the one computation the
design hinges on - which takes about ten minutes, leaving the rest of the session for the
document. The documents go in `designs/`, written to `docs/templates/hld-template.md`.

**Days 88-90 are the capstone** and run against a real Postgres via Testcontainers, so start
Docker Desktop for those.

```powershell
.\day.cmd 81
```
