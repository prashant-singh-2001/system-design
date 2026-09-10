# Day 27 - State + Template Method: workflows and lifecycles

**Phase 3 - Patterns** | 45 minutes

## Concept (10 min)

Day 18's `Order` enforced its lifecycle with guard clauses: every method opened with
`if (status != DRAFT) throw ...`. That works, and it does not scale gracefully - the check gets
repeated in every method, and "which transitions are legal from which state" has to be
reconstructed by reading the whole class.

The **State** pattern represents each state as its OWN object with its own behaviour. An illegal
transition is not a check anyone writes - it is simply what happens when a state's class does
not override the method for it. Look at `PaidState`: it overrides nothing. That absence of code
IS the business rule "a paid order cannot be cancelled." There is no line to misread, no branch
to forget - the rule is enforced by what is NOT there.

`OrderState` also demonstrates **Template Method**: the base class defines the skeleton for
every transition (call the method, get refused unless overridden) and centralises the one thing
that stayed the same across every failure case - building the `"cannot X from Y"` message. A
concrete state overrides only the steps IT varies; the boilerplate of "refuse, with a consistent
message" is written exactly once, in the base class, forever.

## Build (25 min)

In `src/main/java/sd/p03/day27/`:

1. **`DraftState`** - overrides `submit` (require `hasLines`, else throw; otherwise move to
   `SubmittedState`) and `cancel` (always allowed; move to `CancelledState`). Does NOT override
   `pay` - the inherited default already refuses correctly.
2. **`SubmittedState`** - overrides `pay` (-&gt; `PaidState`) and `cancel` (-&gt; `CancelledState`).
3. **`OrderLifecycle`** - the context: holds the current `OrderState`, starts in `DraftState`,
   and delegates every call (`submit`, `pay`, `cancel`) to it, replacing its own field with
   whatever state comes back.

`PaidState` and `CancelledState` are given, and override nothing - read them once you are done
and confirm you understand WHY that emptiness is correct rather than incomplete.

## Reflect (10 min)

1. Rebuild today's four rules as Day 18-style guard clauses in your head. Which version would a
   new teammate get right faster while adding a fifth state - the `if`-chain, or one more
   `OrderState` subclass overriding only what it needs?
2. `OrderState.illegal(action)` builds the refusal message once, in the base class. What would
   duplicating that message-building logic across four subclasses eventually cost, the day
   someone needs to change the wording?
3. State and Day 18's guard-clause aggregate enforce the IDENTICAL set of rules. Name one
   situation where you would deliberately pick the guard-clause version anyway, despite State's
   advantages - what does State cost that guard clauses do not?

**Interview angle:** the strongest answer to "how would you model an order lifecycle" names BOTH
techniques and picks between them on purpose - guard clauses for a handful of states with simple
rules, explicit State objects once the number of states or the rules per transition grows enough
that repeating a check in every method starts to hurt. Naming only one technique reads as having
seen only one codebase.

## Stretch

Add a fifth state, `RefundedState`, reachable only via a new `refund()` transition from `PaidState`
(and refuse `refund()` from everywhere else via the inherited default). Notice exactly one file
needed a new override to add an entire business process - `SubmittedState`, `DraftState` and
`CancelledState` needed no changes at all.

## Checkpoint

```powershell
.\day.cmd 27
```
