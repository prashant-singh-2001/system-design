# Phase 5 - Databases and storage internals

**Days 41-50**

## Why this phase sits here

Docker starts here. From Day 41 you talk to real Postgres, real Redis and real Kafka, because
"the database will handle it" stops being an acceptable answer the moment somebody asks *which
isolation level*.

This is also where Phase 1 pays off. A B-tree is a data structure designed around the cost of a
disk seek. An LSM tree is a data structure designed around sequential writes beating random ones.
You measured both of those costs on Day 1, so these are not arbitrary designs - they are the
obvious consequences of numbers you already know.

## The days

See `CURRICULUM.md` for the full day-by-day list with objectives and deliverables.

## The one idea to carry forward

**Storage engines are Day 1's cost model turned into data structures.** Once you see that,
choosing between them stops being a matter of taste.

---

## Status: code present, briefs incomplete

The starter classes and tests for days 41-50 are under `src/`, and `pom.xml` already pulls in
Testcontainers, the Postgres driver and HikariCP. The `days/` briefs are **not written yet**.

Before starting this phase, ask Claude:

> finish phase 5 - write the day briefs

**Start Docker Desktop before Day 41.** The tests spin up their own disposable Postgres via
Testcontainers, so `mvn test` needs nothing running beforehand except the Docker daemon.
