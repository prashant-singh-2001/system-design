# Day 68 - Event sourcing and CQRS

**Phase 7 - Messaging** | 45 minutes

No Docker today - this is pure Java.

## Concept (10 min)

Event sourcing inverts the usual arrangement. Instead of storing current state and losing how you
got there, you store **every event** and derive current state by replaying them. The log is the
source of truth; everything else is derived.

What that buys is more than it first appears:

- **A complete audit trail, by construction.** Not a log somebody remembered to write - the actual
  storage mechanism. In finance and healthcare this alone justifies the pattern.
- **Time travel.** Replay to any point and see exactly what was true then.
- **New read models from old history.** A question nobody anticipated can be answered by replaying
  events into a new projection. With stored current state, that data is simply gone - and this is
  the strongest argument for the whole approach.
- **Debugging that works.** "How did this account reach a negative balance?" becomes answerable
  rather than a mystery.

**CQRS** is the natural companion: separate the write model (commands producing events) from the
read model (projections answering queries). They can have different shapes, different stores and
different scaling, because a projection is **derived** and can always be thrown away and rebuilt.

Naming matters more than it looks. `Deposited` is a fact; `SetBalance` is a command wearing an
event's clothes. A log of facts can be replayed into any shape you like; a log of commands has
already discarded the reasons.

**The costs, stated honestly:** every query needs a projection. The event schema is forever - you
must still read events written years ago. Replay gets slow without snapshots. And GDPR-style
deletion fights an append-only log by design. Event sourcing is a serious commitment, not a
default.

## Build (25 min)

In `src/main/java/sd/p07/day68/`:

1. `EventStore` - append, `eventsFor(accountId)` in order, `allEvents()`.
2. `BalanceProjection.replay` - fold events into an `AccountBalance`. Return `Optional.empty()`
   for an account with no events; **absent is not the same as zero**, and conflating them is a
   real bug.
3. `replayUpTo` - the same fold over the first n events. Time travel, in one line.

Use a switch over the sealed `AccountEvent`. Because it is sealed (Day 19), the compiler knows
every case, needs no default, and will **refuse to compile** if someone adds an event type without
handling it here. That is exhaustiveness checking doing work a review checklist otherwise has to.

## Reflect (10 min)

1. The last test computed "total deposited" - a question nobody planned for. Explain why it was
   answerable, and why it would not have been with stored current state.
2. Closing an account preserved its balance. Why is "record that something ended" a better model
   than "delete the row"?
3. An account has 2 million events and replay is now slow. What do you add? What does that cost
   you? (You are describing snapshots.)

**Interview angle:** "the events are the source of truth and every read model is a projection we
can rebuild" is the core sentence - and volunteering the costs (schema forever, snapshots,
deletion) is what makes it credible rather than enthusiastic.

## Stretch

Add snapshots: store a balance every 100 events and replay only from the last one. Then work out
what happens when you fix a bug in the projection logic - do your old snapshots still apply?

## Checkpoint

```powershell
.\day.cmd 68
```
