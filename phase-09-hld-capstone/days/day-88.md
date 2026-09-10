# Day 88 - Capstone 1: architecture and skeleton

**Phase 9 - HLD and capstone** | 45 minutes

Start Docker Desktop - the capstone runs against a real Postgres.

## Concept (10 min)

Three days to build a real service, and it is Day 17's URL shortener again. That is deliberate.

On Day 17 you built it against a `HashMap`, with ports you had to take on faith. Today you connect
those ports to a real database, and over the next two days to a real cache and a real circuit
breaker - and the domain will not change by a single line. That non-change is the entire argument
for hexagonal architecture, and you are about to watch it hold.

Today's design decisions, and where each comes from:

- **`code` is the primary key.** The redirect is the hot path and should be a single index probe
  (Day 42).
- **`target_url` is UNIQUE.** That makes the idempotency rule a *database* guarantee rather than an
  application convention, and gives the dedupe lookup an index for free (Day 41).
- **Clicks increment with `UPDATE ... RETURNING`**, never read-modify-write. Day 4's lost update,
  avoided by never bringing the value into Java at all.
- **The clock is injected** (Day 20), so `createdAt` is testable.

The architecture test at the end is the same fitness function as Day 17, now guarding a service
that talks to a real database: **the domain package may not reference the adapter package.** If it
ever does, swapping Postgres becomes a business-logic change, which is precisely what this
structure exists to prevent.

## Build (25 min)

In `src/main/java/sd/p09/capstone/`:

1. `domain/LinkService` - `shorten` (validate, dedupe, generate, save), `resolve`, `recordClick`.
2. `adapter/PostgresLinkRepository` - the four methods, against the given schema. Use the provided
   `prepare(...)` helper so queries are counted; tomorrow's caching tests depend on that count, and
   a cache you cannot measure is a cache you cannot trust.

## Reflect (10 min)

1. `target_url` being UNIQUE enforces idempotency in the database. What happens if two requests to
   shorten the same URL arrive simultaneously? Is that handled?
2. The redirect is one query. What would make it two, and how would you notice in production?
3. Sketch your capstone's architecture diagram now, before adding anything. You will compare it
   with the finished system on Day 90.

## Design note

Start `designs/capstone.md` today with sections 1-4 of `docs/templates/hld-template.md`:
requirements, estimation, API, data model. Assume 100M new links/month at a 100:1 read ratio - the
same numbers you estimated on Day 82, so you can reuse that reasoning and check it against what you
actually built.

## Checkpoint

```powershell
.\day.cmd 88
```
