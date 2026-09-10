# Phase 8 - Reliability and distributed systems

**Days 71-80**

## Why this phase sits here

Everything so far assumed the happy path. This phase removes that assumption: networks
partition, clocks drift, nodes die mid-write, and naive retries turn one slow dependency into a
thundering herd that takes down the whole fleet.

Day 76 implements Raft leader election. Not to memorise a protocol, but because doing it makes
consensus concrete - and after that, every managed service that quietly depends on it reads
differently.

## The days

See `CURRICULUM.md` for the full day-by-day list with objectives and deliverables.

## The one idea to carry forward

**Timeouts, retries and idempotency are one design, not three features.** A retry without a
timeout is a hang; a retry without idempotency is data corruption.

---

## Status: ready

Ten day briefs in `days/`, with starter classes and failing tests under `src/`.

**No Docker needed.** Almost all of this phase is deliberately hand-rolled in plain Java: you
learn far more building a circuit breaker or a Raft election than configuring one. Micrometer
appears on day 78 because instrumentation is that day's actual subject.

```powershell
.\day.cmd 71
```
