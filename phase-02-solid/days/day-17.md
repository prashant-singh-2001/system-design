# Day 17 - Hexagonal architecture

**Phase 2 - SOLID** | 45 minutes

## Concept (10 min)

Also called ports and adapters. One rule:

**Every dependency arrow points inward, toward the domain.**

The domain sits in the middle and contains business rules and nothing else - no HTTP, no SQL, no
Kafka, no framework annotations. It declares **ports**: interfaces describing what it needs
(`LinkRepository`) and what it offers. Outside sit **adapters**: the Postgres repository, the
HTTP controller, the Kafka consumer. Adapters know about the domain. The domain knows nothing
about adapters.

What that buys, concretely:

- Business logic is testable with no infrastructure at all - today's test suite runs in
  milliseconds against real behaviour.
- Storage is swappable. In-memory today, Postgres on Day 41, and the domain does not notice.
- The domain reads like the business. Someone who does not know Java can follow `UrlShortener`.

Note where the idempotency rule lives: in `UrlShortener`, not in the repository and not in a
controller. That is the test for whether something belongs inside the hexagon. Business rules go
in. Everything else is a detail about how the world reaches them.

And note the `Clock`. Time is a dependency exactly like a database is. `Instant.now()` inside a
domain object makes behaviour untestable in precisely the same way `new JdbcDao()` did
yesterday, and for the same reason.

**The trade-off:** more packages, more interfaces, and a trivial CRUD endpoint now spans three
layers to do almost nothing. Hexagonal architecture pays off when the domain is genuinely
complex or the infrastructure genuinely changes. For a service that reads a row and returns
JSON, it is ceremony - and pretending otherwise is how the pattern got its reputation.

## Build (25 min)

In `src/main/java/sd/p02/day17/`:

- `domain/UrlShortener` - constructor takes `LinkRepository`, `CodeGenerator` and `Clock`.
  `shorten` rejects anything not `http://` or `https://`, returns the existing link if the URL
  was already shortened, otherwise generates and saves. `resolve` looks a code up.
- `adapter/InMemoryLinkRepository` - a map keyed by code.
- `adapter/SequentialCodeGenerator` - base-62 over an `AtomicLong` starting at 0, so the first
  codes are `"0"`, `"1"`, `"2"`.

The last test is an **architecture fitness function**: it scans the domain sources and fails if
any of them mention the adapter package. Structural rules are worth enforcing automatically -
code review does not scale, and a rule nobody checks is a rule nobody follows.

## Reflect (10 min)

1. Which files change when storage moves from a map to Postgres? Which do not?
2. The idempotency rule sits in the domain. Argue for putting it in the repository instead, then
   say why the domain is the better home.
3. Sequential codes are compact and collision-free, but guessable, and they leak your total
   volume to anyone who reads one. What would you use instead, and what would that cost?

**Interview angle:** "the domain declares ports and adapters implement them, so swapping storage
touches no business logic" is a strong sentence in any design discussion - especially followed
by an honest "for a CRUD service I would not bother".

## Stretch

Add a second adapter: a `HashCodeGenerator` that derives the code from a hash of the URL. Notice
that idempotency now comes for free - and think about what happens on a hash collision.

## Checkpoint

```powershell
.\day.cmd 17
```
