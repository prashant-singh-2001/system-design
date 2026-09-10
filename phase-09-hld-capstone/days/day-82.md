# Day 82 - Design: URL shortener

**Phase 9 - HLD and capstone** | 45 minutes

## Concept (10 min)

The classic opener, and it is deceptively deep. The whole design turns on one question: **how do
you generate a short code?**

Three strategies, three different systems:

- **Counter, base-62 encoded.** Shortest possible codes, zero collisions. Costs you a distributed
  counter - a coordination point - and it **leaks your volume**: anyone can decode a code and
  watch the number climb. Adjacent codes are adjacent links, so your entire corpus is enumerable.
- **Hash the URL, truncate.** Idempotency for free - the same URL always gets the same code - but
  collisions are real and must be detected and handled.
- **Random, checked for uniqueness.** Unguessable and uncorrelated. Costs a uniqueness check per
  generation and longer codes to keep collisions rare.

Today's kernel makes the collision maths concrete, and the result is counter-intuitive. Because of
the birthday paradox, **a million random 6-character codes in a 57-billion space collide with near
certainty.** The intuition is wrong by orders of magnitude, which is exactly why you compute it
rather than estimate it.

The other thing worth internalising: this is one of the most **read-heavy** systems there is.
Roughly 100:1 or higher - links are created once and followed many times. That single ratio
dictates almost everything downstream: cache aggressively, replicate reads, and make the redirect
path a single index probe.

And the redirect itself is a design decision people skip: **301 or 302?** A 301 is permanent, so
browsers cache it and you never see the request again - excellent for load, fatal for analytics. A
302 costs you every redirect as real traffic and keeps the click data. If the product sells
analytics, that is not a technical decision at all.

## Build (25 min)

**First (about 10 min)** implement `ShortCodeMath` in `src/main/java/sd/p09/day82/`: base-62
capacity, the birthday collision approximation, the shortest safe length, and counter encoding.

**Then (about 15 min)** write `phase-09-hld-capstone/designs/url-shortener.md` using
`docs/templates/hld-template.md`. Assume 100M new URLs/month and a 100:1 read ratio.

Do not skip the estimation section - it is what makes the rest defensible. And make sure your deep
dive picks *one* thing and goes deep, rather than touching everything shallowly.

## Reflect (10 min)

1. Which key-generation strategy would you ship, and what is the one question you would ask the
   product owner before deciding?
2. Custom aliases (`/my-brand`) collide with generated codes. How do you keep both in one key
   space without ugly failures?
3. Links expire. Where does the deletion happen, and what does it do to your cache?

**Interview angle:** get to the code-generation trade-off in the first two minutes. "Counter is
densest but enumerable; random is unguessable but needs a collision check - which matters more
here?" turns the interviewer into a collaborator and shows you know where the design actually
lives.

## Stretch

Add analytics: clicks per link, per day, per country. Now revisit your 301-versus-302 decision, and
notice that a purely technical optimisation has just deleted a product feature.

## Checkpoint

```powershell
.\day.cmd 82
```
