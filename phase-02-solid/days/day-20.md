# Day 20 - Phase review: the refactor kata

**Phase 2 - SOLID** | 45 minutes

## Concept (10 min)

Start by rereading your `NOTES.md` from days 11-19. Five minutes, properly - this is the
retrieval practice that makes the phase stick.

Then today's real subject, which is not a principle but a **technique**: how do you safely
change code you did not write and do not fully understand?

The answer is **characterization tests**. Before touching anything, you write tests that
describe what the code does *today* - correct or not, sensible or not. They are not
specifications; they are a net. Once they are green, you can restructure freely, and any
behaviour change shows up immediately as a red test.

This is the definition of refactoring, and it is stricter than common usage: **change the
structure, never the behaviour.** If behaviour changes, it is a rewrite, and a rewrite needs a
different conversation with your team and usually a different release plan. Conflating the two is
how "just a small refactor" becomes a three-week incident.

`Day20CharacterizationTest` is given and already green. It runs each scenario against both the
legacy and the refactored service and asserts they agree.

`LegacyLibraryService` contains every problem from this phase: inventory, lending and pricing in
one class (SRP), a hard-coded fee rate (OCP), self-constructed storage and a direct call to
`LocalDate.now()` (DIP). That last one is why you cannot test a late fee without waiting three
weeks. Time is a dependency exactly like a database is.

## Build (25 min)

In `src/main/java/sd/p02/day20/`:

1. `FeePolicy` - one method, `long feeCents(long daysLate)`.
2. `StandardFeePolicy` - 50 cents per day late, zero if not late, never negative.
3. `LibraryService(Clock, FeePolicy)` - identical public behaviour, same exception messages.
   Use `LocalDate.now(clock)`, never `LocalDate.now()`.

Then watch `Day20RefactorTest` go green. Every test in it was impossible against the legacy
design - including a five-days-late fee computed in a millisecond, and a brand-new pricing
policy that `LibraryService` has never heard of.

If you have time left, extract inventory into its own type. If you do not, stop. The clock and
the fee policy are the two changes that matter most, and finishing two well beats starting four.

## Reflect (10 min)

1. Which principle produced the biggest payoff here, and what makes you say so?
2. The characterization tests pinned existing behaviour including anything odd about it. When
   would you deliberately *not* preserve behaviour, and how would you handle that differently?
3. `LibraryService` still owns inventory and lending. Is that a violation? What would you split,
   and what would that cost in indirection?

## Phase 2 retrospective

Write in `NOTES.md`:

- The one principle that changed how you look at your own code
- The one you think is most **over**-applied in practice, and why
- A class in a codebase you work on that violates one of these. Which, and what would you do?

Then tick days 11-20 in `PROGRESS.md` and commit.

## Looking ahead

Phase 3 is patterns - but only the ones that reappear when you draw distributed systems. You
have already built two of them without the names: the strategy registry on Day 12, and ports and
adapters on Day 17. That is deliberate. The patterns are not new ideas; they are names for
shapes you now recognise.

## Checkpoint

```powershell
.\day.cmd 20
```
