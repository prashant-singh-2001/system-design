# Phase 3 - Patterns that actually appear in system design

**Days 21-30**

## Why this phase sits here

You have already built two of these without the names: the strategy registry on Day 12, and
ports and adapters on Day 17. That is deliberate. Patterns are not new ideas, they are names for
shapes you now recognise - and a shared name is what lets four people agree on a design in a
five-minute conversation.

This phase covers roughly eight of the twenty-three, chosen by one criterion: they reappear when
you draw distributed architectures. A middleware chain *is* Decorator. Pub/sub *is* Observer. An
anti-corruption layer *is* Adapter. Learning them at object scale first means recognising them at
system scale later.

## The days

See `CURRICULUM.md` for the full day-by-day list with objectives and deliverables.

## The one idea to carry forward

**The same shape appears at every scale.** A decorator wrapping a method and a sidecar proxy
wrapping a service are the same idea. Notice the recurrence and system design gets much smaller.

---

## Status: ready

Ten day briefs in `days/`, with starter classes and failing tests under `src/`.

```powershell
.\day.cmd 21
```
