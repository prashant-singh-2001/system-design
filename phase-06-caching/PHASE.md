# Phase 6 - Caching and load distribution

**Days 51-60**

## Why this phase sits here

A cache is the cheapest scalability win available and the richest source of production
incidents. This phase builds one, breaks it, and fixes the specific ways it breaks at scale -
stampedes, invalidation, hot keys.

Consistent hashing on Day 56 is the centrepiece. It is the answer to a question that sounds
trivial - which node holds this key? - and modulo hashing gets it wrong in a way that costs you
the entire cache every time you add a machine.

## The days

See `CURRICULUM.md` for the full day-by-day list with objectives and deliverables.

## The one idea to carry forward

**Every cache is a bet that stale data is cheaper than a slow answer.** Say what your staleness
budget is, out loud, or you have not designed a cache - you have added one.

---

## Status: ready

Ten day briefs in `days/`, with starter classes and failing tests under `src/`.

**Days 51, 55 and 58 talk to a real Redis** via Testcontainers, so start Docker Desktop before
those. The other seven are pure Java - the lesson there is the algorithm, not the client library.

```powershell
.\day.cmd 51
```
