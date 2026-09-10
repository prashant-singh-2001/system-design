# Day 31 - A repeatable LLD method that survives time pressure

**Phase 4 - Low-level design craft** | 45 minutes

## Concept (10 min)

Open `docs/templates/lld-template.md` now, before reading further. Seven sections, each with a
time budget summing to 45 minutes: requirements, core entities, public API, class design,
concurrency, tests, trade-offs and extensions.

The method exists because an unstructured 45 minutes reliably goes wrong in one of two ways.
Some people start coding in minute two, before the entities are even named, and spend the next
forty minutes discovering requirements one compile error at a time. Others spend thirty-five
minutes gathering requirements so thoroughly that the actual design gets five rushed minutes at
the end. The template's time budget is the fix for both: it forces you OFF requirements at
minute five whether you feel done or not, and it guarantees class design and the API get their
fair share of the clock.

Notice something else about the order: **concurrency has its own section**, always, even for a
single-threaded design. "What is shared, and what is the locking strategy" is a question worth
asking on purpose rather than discovering by accident three days before a production incident -
and "single-threaded by design, here is why that is sufficient" is a completely legitimate
answer, the template just wants you to have SAID it rather than silently skipped it.

## Build (25 min)

Today's system is `MinStack` - deliberately tiny, because the point of today is running the FULL
method fast, not spending the method's budget on a hard problem. Implement `push`, `pop`, `top`
and `getMin`, all O(1), using two parallel stacks (the second tracking the running minimum at
each point in the first's history - see the class javadoc for the exact mechanic).

## Reflect (10 min)

Before you write any code, actually run the template - timed, on paper or in
`docs/notes/day-31-lld-warmup.md` - against `MinStack`. Yes, even though the problem is small.
Fill in sections 1-4 (requirements, entities, API, class design) in about ten minutes, THEN
implement against your own plan, THEN fill in sections 5-7 once you have felt where the
concurrency question and the trade-offs actually are for this problem.

1. Which section did you find hardest to keep to five or ten minutes, for a problem this small?
   That is worth noticing now, because it will be worse under a real prompt.
2. `MinStack` is single-threaded in this exercise. What WOULD have to change - specifically - to
   make `push`/`pop`/`getMin` safe if two threads shared one instance?
3. The trade-offs section asks what you deliberately did not build. For `MinStack`, what is the
   honest answer?

**Interview angle:** naming the seven sections from memory, out loud, in order, at the start of
an LLD round is a small thing that buys you a lot: it signals structure to the interviewer
immediately, and it means you never have to stop and think "what should I be doing right now."

## Stretch

Time yourself running the template against a SECOND tiny system - a bounded circular buffer, or
a simple undo/redo command stack (Day 26's Command pattern, reused). Compare your section timings
to `MinStack`'s. Are you getting faster at the same sections, or different ones?

## Checkpoint

```powershell
.\day.cmd 31
```
