# Day 81 - The 45-minute framework

**Phase 9 - HLD and capstone** | 45 minutes

## Concept (10 min)

Interviewers are not assessing whether you know what a load balancer is. They are assessing
whether you can take an ambiguous problem, impose structure on it, make decisions under time
pressure, and defend them.

**Structure is most of the score, and it is the part you can practise.**

| Minutes | Phase | What good looks like |
|---|---|---|
| 0-5 | **Scope** | Ask 3-4 sharp questions, then *state the scope yourself* and get agreement |
| 5-10 | **Estimate** | QPS, storage, read:write ratio - and say which numbers will drive design |
| 10-15 | **API + data model** | Signatures and the partition key, with justification |
| 15-25 | **Architecture** | Draw it, then trace one read and one write through the boxes |
| 25-35 | **Deep dive** | The genuinely hard part. This is most of your score |
| 35-45 | **Bottlenecks** | What breaks at 10x, what happens when each box dies |

The single most common failure is spending 25 minutes on requirements and never drawing the
system. The parts you are actually scored on - the deep dive and the failure analysis - then never
happen. Manage the clock out loud: *"I have used ten minutes, let me move to the architecture"* is
a strong signal on its own, because it says you are running the session rather than being carried
by it.

Phrases that signal seniority, and why:

- *"I am going to assume X - tell me if that is wrong."* Unblocks yourself instead of waiting.
- *"That is out of scope for now; I will come back if there is time."* Scoping is a senior skill.
- *"This is read-heavy at 50:1, so I will optimise the read path first."* Numbers to decisions.
- *"The trade-off is consistency against latency. Given the requirement, I choose..."* Names the axis.
- *"This will break first when..."* Knowing your own design's limits.

And the opposites: naming a tool before stating the problem, "it will scale" with no mechanism,
listing every technology you know, and - the quietest one - thinking in silence. Narrate. They are
assessing reasoning, not answers.

**Start from the default architecture and justify each deviation**, rather than from a blank page:

```
Client -> CDN -> Load Balancer -> Service -> Cache
                                     |          |
                                  Database    Queue -> Workers -> Blob storage
```

## Build (25 min)

Implement `DesignClock` in `src/main/java/sd/p09/day81/`. Small, and the point is that the phases
must sum to exactly 45 minutes - a plan whose parts do not add up is a wish.

Then read `docs/cheatsheets/hld-framework.md` and rehearse the sequence out loud once, against any
system you like. Ten minutes of that is worth more than an hour of reading.

## Reflect (10 min)

1. Which phase are you most likely to overrun? What is your specific plan for cutting it short?
2. Write the four questions you would ask at minute two for *any* design. They should be
   near-universal.
3. `isBehind` detects the classic failure. What would you actually say out loud at minute 25 if
   you were still scoping?

**Interview angle:** the framework itself is the angle. Announcing it at the start - "I will spend
five minutes on requirements, five on estimates, then draw the architecture" - takes fifteen
seconds and reframes you as someone who has done this before.

## Stretch

Time yourself on a design you have never seen. Do not aim for a good answer; aim to hit every
phase boundary. Getting through all six badly beats doing two of them well, because the last two
carry the marks.

## Checkpoint

```powershell
.\day.cmd 81
```
