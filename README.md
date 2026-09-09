# 90-Day System Design Curriculum — Java

Learn system design by building it. Ninety 45-minute sessions that run from how a single
machine actually behaves, through SOLID and low-level design, into caching, sharding,
messaging and consensus, and out the far end with high-level designs written the way an
interviewer expects to read them.

Every day ends with a **green test**. Not a video watched, not a page read — a test.

---

## The daily ritual (45 minutes)

| Block | Time | What you do |
|---|---|---|
| **Concept** | 10 min | Read `phase-NN/days/day-NN.md`. Every concept is framed as a trade-off, because that is what you get asked about. |
| **Build** | 25 min | Make the failing test pass. The starter class and the red test are already committed. |
| **Reflect** | 10 min | Answer the three questions in your `NOTES.md`. Skipping this is skipping the learning. |

If the Build block overruns, stop at 25 minutes anyway and read the reference solution
approach in the brief. Consistency beats completeness — a finished day at 70% understanding
beats an unfinished day at 100%.

## Running a day

```powershell
.\day.cmd 7          # prints Day 7's brief, then runs Day 7's tests
.\day.cmd 7 -Brief   # just the brief
.\day.cmd 7 -Test    # just the tests
```

To build everything:

```powershell
mvn -q clean test                       # whole repo
mvn -q -pl phase-01-foundations test    # one phase
```

### Why `day.cmd` and not `day.ps1`

`day.cmd` is a two-line wrapper around `day.ps1`, and it exists because of PowerShell's execution
policy. Windows PowerShell 5.1 on this machine is set to **`AllSigned`** at machine scope, so it
refuses to run any unsigned script - including one you wrote yourself thirty seconds ago.
PowerShell 7 is set to `RemoteSigned` and runs it happily.

Batch files are not subject to execution policy at all, so the wrapper works from cmd.exe, from
either PowerShell edition, and from a double-click - without changing a single machine setting. It
prefers `pwsh` when present and falls back to Windows PowerShell.

If you would rather relax the policy for your own account instead, this does it with no admin
rights and no effect on other users:

```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
```

That is your call - `AllSigned` is a hardened setting and may have been applied deliberately. The
wrapper means you do not have to decide.

Either way, `day.ps1` still resolves Java through `$env:JAVA_HOME`: the `java` on your `PATH` is a
stale JRE 8, and this project targets Java 21.

## Prerequisites

- **JDK 21+** on `JAVA_HOME` (you have JDK 25 — good)
- **Maven 3.9+**
- **Docker Desktop** — not needed until Day 41, but start it before Phase 5

## Layout

```
CURRICULUM.md        all 90 days: objective + deliverable      <- the map
PROGRESS.md          tick a box each day
day.ps1              the daily runner
docs/                templates (ADR, LLD, HLD, daily note) + cheatsheets
infra/               docker-compose stack: postgres, redis, kafka, nginx, prometheus, grafana
phase-NN-*/          one Maven module per phase
  PHASE.md           what this phase teaches and why it sits here
  days/day-NN.md     the 45-minute briefs
  src/main/java/sd/pNN/dayNN/    your code
  src/test/java/sd/pNN/dayNN/    the tests that must go green
```

## Real infrastructure, without the setup tax

From Phase 5 onward you work against **real** Postgres, Redis and Kafka — not fakes. Two
mechanisms keep that inside a 45-minute box:

1. **Testcontainers.** Infra-backed tests start their own container. `mvn test` remains the
   only command you type; no manual setup, no leftover state between days.
2. **`infra/docker-compose.yml`** for the days you want to poke at things by hand — `psql`,
   `redis-cli`, a Kafka console consumer, Grafana dashboards.

```powershell
docker compose -f infra/docker-compose.yml up -d
docker compose -f infra/docker-compose.yml down -v
```

## A note on frameworks

Phases 1–8 are **plain Java + JUnit 5**. No Spring. Spring is excellent and you will use it at
work, but it hides precisely the mechanics this curriculum is trying to make visible — connection
pooling, retries, serialization, the request pipeline. Build them by hand once and the framework
version stops being magic. Spring Boot is offered as an option for the Phase 9 capstone only.

## Progress

Tick `PROGRESS.md` as you go, and commit at the end of each day. The commit log becomes a
record of what you actually understood on which day — far more useful than notes alone.

## What is ready right now

All ninety days: briefs, starter code and failing tests.

| Phase | Days | Needs Docker |
|---|---|---|
| 1 Foundations | 1-10 | no |
| 2 SOLID | 11-20 | no |
| 3 Patterns | 21-30 | no |
| 4 LLD | 31-40 | no |
| 5 Databases | 41-50 | **yes** - Postgres |
| 6 Caching | 51-60 | days 51, 55, 58 - Redis |
| 7 Messaging | 61-70 | days 61-65, 70 - Kafka + Postgres |
| 8 Reliability | 71-80 | no |
| 9 HLD + capstone | 81-90 | days 88-90 - Postgres |

Infra-backed days start their own containers via Testcontainers, so `mvn test` stays the only
command you type - the Docker daemon just has to be running.
