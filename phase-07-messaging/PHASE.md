# Phase 7 - Async, messaging and streams

**Days 61-70**

## Why this phase sits here

Synchronous calls couple availability: chain five services and your uptime is the product of
theirs. Messaging is how you break that chain, and it is where you finally have to be precise
about delivery semantics.

The outbox pattern on Day 65 is the one to get right. It answers the question every event-driven
system eventually asks: how do you write to your database and publish an event atomically, when
they are two different systems and either can fail?

## The days

See `CURRICULUM.md` for the full day-by-day list with objectives and deliverables.

## The one idea to carry forward

**Exactly-once delivery does not exist. Exactly-once *processing* does** - through
idempotency. Anyone selling you the first is selling you the second with worse marketing.

---

## Status: ready

Ten day briefs in `days/`, with starter classes and failing tests under `src/`.

**Days 61-65 and 70 drive a real Kafka** through Testcontainers (day 65 and 70 add a real
Postgres), so start Docker Desktop before those. Days 66-69 are pure Java - backpressure,
dead-letter handling, event sourcing and windowing are patterns, not broker features.

The first Kafka run pulls a container image and takes a couple of minutes. After that it is
seconds.

```powershell
.\day.cmd 61
```
