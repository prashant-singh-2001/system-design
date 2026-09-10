# Day 36 - Explicit state machines beat scattered boolean flags

**Phase 4 - Low-level design craft** | 45 minutes

## Concept (10 min)

Open `LegacyVendingMachine`. Two independent booleans, `hasCoins` and `isDispensing`, are meant
to represent one underlying idea - what mode is this machine currently in - but nothing LINKS
them. Today, in this small, careful class, the impossible combination
(`hasCoins == false && isDispensing == true`) never actually arises. That is not evidence the
design is safe; it is evidence the class has not been touched by a second feature yet. Add a
third boolean for a promotions feature, in a hurry, without re-reading every existing method, and
every one of eight combinations becomes reachable - most of them nonsense.

The fix is not the full State pattern from Day 27 - it is smaller and more broadly applicable:
**one authoritative field, `MachineState`, checked explicitly wherever it matters.** "Impossible"
states become impossible not because a class hierarchy prevents them (Day 27's mechanism) but
because there is no SECOND field left to disagree with the first.

The other half of today is keeping two different kinds of "no" apart, which is the Day 19 lesson
applied to a state machine: calling `selectItem` before inserting coins is a PROTOCOL violation -
a caller bug, an exception. Selecting a valid code that happens to be out of stock, or paying
too little, are everyday BUSINESS outcomes - modelled as data (`SelectionResult`), because a
customer choosing an empty slot is not a bug in anyone's code.

## Build (25 min)

In `src/main/java/sd/p04/day36/`, implement `VendingMachine`: one `MachineState` field, an
inventory of `Item`s, `insertCoin` (validate positive, accumulate, move to `HAS_COINS`),
`selectItem` (guard `IDLE`, then unknown/out-of-stock/insufficient-funds as `SelectionResult`
values, or a real dispense that decrements stock and returns to `IDLE`), and `refund` (guard
`IDLE`, return and zero the balance).

## Reflect (10 min)

1. `protocolViolationsAreRejected` throws for `selectItem`/`refund` from `IDLE`.
   `insufficientFundsStaysInHasCoins` does NOT throw for an underfunded selection. State the
   general rule, in one sentence, for sorting a "no" into one bucket or the other.
2. `LegacyVendingMachine` has two booleans and gets away with it. At roughly what number of
   independent flags does "just add another boolean" stop being a reasonable choice, in your own
   judgement - and what is the actual failure mode once you cross that line?
3. Compare today's single-enum-field approach with Day 27's polymorphic `OrderState` classes.
   Both make illegal transitions impossible. What does today's version cost less of, and what
   does it lose compared to Day 27's version once the number of states grows past a handful?

**Interview angle:** "I'd use an enum for the state" is the right instinct, incompletely stated.
The signal is explaining WHY scattered booleans fail - specifically, that they can independently
drift into combinations nobody intended - rather than just asserting that enums are tidier. Being
able to point at `LegacyVendingMachine` and name the exact unreachable-but-not-prevented
combination is the concrete version of that argument.

## Stretch

Add a `MAINTENANCE` state, reachable only via a new `enterMaintenance()` call from `IDLE`, from
which every other operation (`insertCoin`, `selectItem`, `refund`) throws. Confirm you touched
one field's enum values and a handful of guard checks - not a redesign - to add a whole new mode.

## Checkpoint

```powershell
.\day.cmd 36
```
