# Day 43 - Isolation levels, reproduced

**Phase 5 - Databases** | 45 minutes

## Concept (10 min)

Everyone can recite the four isolation levels. Almost nobody has *seen* the anomalies. Today you
trigger them on purpose, on two real, separate JDBC connections, which is the only way this
knowledge becomes usable under pressure.

The three classic anomalies:

- **Dirty read** - you read data another transaction has written but not committed. It may roll
  back, so you read something that never existed.
- **Non-repeatable read** - you read a row twice in one transaction and get different values,
  because someone committed an update in between.
- **Phantom read** - you run the same *query* twice and get a different number of rows, because
  someone committed an insert matching your predicate.

The isolation levels are defined by which of these they forbid. But the genuinely useful lesson
today is that **the SQL standard describes a minimum, and real databases differ**:

- Postgres has no true `READ UNCOMMITTED`. Ask for it and you get `READ COMMITTED`. A dirty read
  simply cannot happen, whatever you configure.
- Postgres's `REPEATABLE READ` also prevents phantom reads, which the standard does not require.
  It is implemented as snapshot isolation, and a snapshot does not grow new rows.

So "we use REPEATABLE READ" means something different on Postgres than on MySQL. Answering an
interview question with the standard's table, when the system in front of you behaves
differently, is exactly the kind of confident-but-wrong that gets noticed.

**The trade-off:** stronger isolation costs concurrency and introduces serialization failures
your application must be prepared to retry. `READ COMMITTED` is the default because it is the
level most workloads can tolerate; stronger levels are a deliberate purchase, made per
transaction, not a global setting you raise because stronger sounds safer.

## Build (25 min)

In `src/main/java/sd/p05/day43/IsolationLab.java`, reproduce all three anomalies using two real
connections:

1. **Dirty read attempt** - writer updates without committing, reader reads, writer rolls back.
2. **Non-repeatable read** - reader reads, writer commits an update, reader reads again.
3. **Phantom read** - reader counts, writer commits an insert, reader counts again.

Each runs at a configurable isolation level, and the test asserts which level allows what.

The sequencing is the fiddly part: both connections are live at once, and the steps must
interleave in exactly the right order. That difficulty is the point - it is why these bugs are
so hard to find in production, where the interleaving is accidental rather than arranged.

## Reflect (10 min)

1. Which anomaly could not be reproduced at all on Postgres, and why?
2. Postgres's `REPEATABLE READ` blocked phantoms even though the standard permits them. Name one
   way relying on that would hurt you.
3. Your service reads a balance, decides, and writes. Which isolation level do you need, and what
   must the application do when the transaction fails to serialize?

**Interview angle:** "we would use `SELECT ... FOR UPDATE` at READ COMMITTED, or REPEATABLE READ
with a retry on serialization failure" is a strong answer, because it names the retry. Isolation
without a retry strategy is half a design.

## Stretch

Add a fourth scenario: **write skew**. Two transactions each read a shared invariant, each decide
their write is safe, and together they break it. Watch `REPEATABLE READ` permit it and
`SERIALIZABLE` refuse it. This is the anomaly snapshot isolation cannot fix, and it is the
strongest argument for `SERIALIZABLE` existing.

## Checkpoint

```powershell
.\day.cmd 43
```
