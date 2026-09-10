# Day 46 - Model for the access pattern

**Phase 5 - Databases** | 45 minutes

## Concept (10 min)

"SQL or NoSQL" is the wrong question, and it is worth being able to say why.

Relational modelling asks *what is true about this domain* and normalizes until each fact lives
once. Wide-column and document modelling ask *what does this screen need* and store it that way,
duplicates and all. The first optimises for writes and for change; the second optimises for a
specific read.

Today you build the same feature both ways: a user's profile plus their five most recent orders.

- **Normalized:** two queries, or one JOIN. Writing an order is one insert. Nothing is ever
  inconsistent, because nothing is duplicated.
- **Denormalized:** one query against a pre-built view holding the profile and a trimmed array of
  recent orders. Writing an order is now *two* writes - the order itself, and the prepend-and-trim
  on the view.

The test asserts exactly that asymmetry: the denormalized read takes one round trip, and the
denormalized write costs two. **You bought read latency with write cost and a consistency
obligation.** That sentence is the entire lesson, and it generalises far beyond this exercise -
it is the same bargain as a cache (Phase 6) and the same bargain as fanout-on-write (Day 10).

The consistency obligation is the part people underestimate. Two writes are not atomic unless you
make them so. If the second fails, the view is now wrong, and nothing will tell you. That problem
has a name and a solution you will build on Day 65: the outbox pattern.

**The trade-off:** denormalization is not "NoSQL". You can denormalize inside Postgres - which is
exactly what today does, with array columns. Choosing a different database is a much bigger
decision than choosing a different shape, and conflating the two is how teams end up migrating
to Cassandra when they needed a materialized view.

## Build (25 min)

In `src/main/java/sd/p05/day46/UserProfileService.java`:

1. **`loadNormalized`** - one query for the user, one for their five most recent orders.
2. **`loadDenormalized`** - one query against `user_profile_view`, reading the array columns.
3. **`recordOrder`** - insert into `orders`, then prepend-and-trim both arrays on the view.

Both load paths must return identical results - the test compares them directly - while the
query counts differ. Same answer, different cost profile.

## Reflect (10 min)

1. Write down the read cost and the write cost of each model, in round trips.
2. `recordOrder` does two writes. What is the state of the system if the second one fails? How
   would you notice, and how long would it stay wrong?
3. The denormalized view keeps only the five most recent orders. What happens when the product
   team asks for ten? Compare that migration with the normalized model's.

**Interview angle:** "this is a read-heavy path at 50:1, so I would keep the normalized tables as
the source of truth and maintain a denormalized read model beside them, updated through an
outbox" is a complete position. It names the source of truth, which is the thing that actually
matters.

## Stretch

Add a `rebuildView(userId)` that regenerates the denormalized row from the normalized tables. Now
you have a repair mechanism - and repairability is what makes a denormalized model safe to
operate rather than merely fast.

## Checkpoint

```powershell
.\day.cmd 46
```
