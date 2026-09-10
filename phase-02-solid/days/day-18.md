# Day 18 - Aggregates and invariants

**Phase 2 - SOLID** | 45 minutes

## Concept (10 min)

Two ideas that get conflated, and separating them is most of domain modelling:

- A **value object** has no identity. Two `Money` instances with equal fields *are* the same
  thing. It should be immutable. That was yesterday.
- An **entity** has identity and a lifecycle. Order `ord-1` is the same order today and tomorrow,
  even after its contents change. It may be mutable - but every mutation must leave it valid.

An **aggregate** is a cluster of entities and value objects treated as one unit for consistency,
with a single **root** as the only way in. Nothing outside may hold a reference to an internal
line and change it. That is what makes invariants enforceable: every path that could break them
goes through methods you control.

Compare the alternative, which is what most codebases actually do: scatter
`if (order.getStatus() == DRAFT)` across five services and hope nobody forgets. That is not an
invariant, it is a convention with good intentions. Someone always forgets, usually in the one
service written under deadline.

The payoff here is that **an invalid order cannot exist, even briefly**. No caller has to
remember to check the status before adding a line, because there is no way to get it wrong.

Two smaller decisions worth noticing:

- Illegal *state transitions* throw `IllegalStateException`; illegal *data* throws
  `IllegalArgumentException`. Different failures, different types.
- A PAID order cannot be cancelled. That is not an oversight - refunding is a different process
  with different rules and different authorisation. Encoding that in the type is a business
  decision made visible.

**The trade-off:** aggregates set your transaction boundary, and a big aggregate means a big lock
and a contention hot spot. This is the same "shard the contended thing" tension from Day 5, at
the domain level. Design aggregates small; anything that does not need to be transactionally
consistent belongs in a different one.

## Build (25 min)

Implement `Order` in `src/main/java/sd/p02/day18/`. The full invariant list is in the class
javadoc - eight rules. `lines()` returns an unmodifiable view, because an invariant you can
bypass from outside is not an invariant.

## Reflect (10 min)

1. Where would each rule have lived without an aggregate? How many places could forget it?
2. Argue for making `Order` fully immutable, with every operation returning a new instance. What
   would you gain, and what would get harder?
3. Should `OrderLine` be an entity or a value object? What decides?

**Interview angle:** "I would make Order the aggregate root, so status transitions and line
edits are enforced in one place and the transaction boundary is one order." That sentence tells
an interviewer you have thought about both consistency and locking - which are the same
question wearing different clothes.

## Stretch

Add domain events: `submit()` records an `OrderSubmitted`, exposed via `pullEvents()`. This is
the foundation of the outbox pattern you will build on Day 65 - the aggregate records what
happened, and something else decides how to publish it.

## Checkpoint

```powershell
.\day.cmd 18
```
