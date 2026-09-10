# Day 90 - Capstone 3: prove it, then reflect

**Phase 9 - HLD and capstone** | 45 minutes

Start Docker Desktop. Last one.

## Concept (10 min)

Today you measure what you built and write the document that closes the programme.

Every decision in the load test is one you have already argued through:

- **A Zipfian workload** (Day 60), because a uniform key distribution makes any cache look useless
  and real traffic is never uniform. Choosing a representative workload is most of the work in
  benchmarking, and the most common place a benchmark quietly lies.
- **Percentiles, not averages** (Day 60 again). The mean describes nobody's experience, and once
  you have averaged you can never recover the tail.
- **Availability against a stated SLO** (Day 79), because "it felt fast" is not a result.
- **A degraded response counts as a failure**, because it does to the user. Dashboards that count
  it as a success are how outages go unnoticed.

And the result you should expect, which is the course's own conclusion arriving one last time:
**p50 collapses and p99 barely moves.** Misses still pay the full database round trip. A cache
fixes a *busy* dependency, not a *slow* one. Being able to say that - rather than "we added a cache
and it got faster" - is the difference these ninety days were for.

## Build (25 min)

Implement `LoadTest.run` in `src/main/java/sd/p09/day90/`. Then watch the summary line print
against a real Postgres, through your cache and your breaker, and check it against the three-nines
target.

## Finish the design document (the rest of the session)

Complete `designs/capstone.md` with section 7 - bottlenecks, failure modes and scaling - and add a
results section with the actual numbers you measured. A design document with real measurements in
it is worth ten without.

Then answer these three, in the document:

1. **What breaks first at 10x?** Name the component and the number at which it breaks.
2. **What happens when each box dies?** Go through them one at a time. You have tested two of these
   for real.
3. **What would you do differently?** Now that it exists, what was the wrong call?

## Programme retrospective

This is the last entry in `NOTES.md`, and it is the one you will actually reread. Write:

- **The five ideas that changed how you think.** Not the five topics - the five ideas.
- **The three numbers you now know by heart**, and what each one decides.
- **The one thing you would tell yourself on Day 1.**
- **What is still fuzzy**, and what you will do about it.

Then tick days 81-90 in `PROGRESS.md` and make the final commit.

## What you have

Ninety sessions. Roughly eight thousand lines of Java. Real Postgres, Redis and Kafka. Seven design
documents and a working service.

More usefully: you have measured the memory hierarchy, built an LSM tree, caused a deadlock on
purpose, reproduced a cache stampede, implemented consistent hashing, run a Kafka consumer group
through a rebalance, built a transactional outbox, elected a Raft leader, defeated a zombie writer
with a fencing token, and hardened a service until it held its SLO through a partial outage.

None of that is vocabulary any more. When somebody asks how you would design something, you are
choosing between things you have used - and that is what this was for.

## Where to go next

- **Reread your design documents** before any interview. Not to memorise answers, but to remember
  which trade-offs you already argued through and what numbers you used.
- **The books that go deeper**: Kleppmann's *Designing Data-Intensive Applications* for the theory,
  Google's *SRE Book* for the operational half.
- **The habit that matters most**: for every design you meet at work, ask "what breaks first, and
  at what number?" If you cannot answer, you have found the next thing to learn.

## Checkpoint

```powershell
.\day.cmd 90
```
