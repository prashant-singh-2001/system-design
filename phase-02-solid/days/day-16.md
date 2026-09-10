# Day 16 - Value objects and immutability

**Phase 2 - SOLID** | 45 minutes

## Concept (10 min)

A `long` holding pence is a number. `Money` is a concept. The difference is that the concept can
enforce its own rules, and the number cannot.

Three rules `Money` exists to enforce:

1. **Never use floating point for money.** `0.1 + 0.2 != 0.3` in binary floating point. Store
   minor units - cents, pence, satoshi - as whole numbers and the problem disappears. This is
   not pedantry; it is a recurring and expensive class of financial bug.
2. **Currency is part of the value.** 100 USD plus 100 EUR is not 200 of anything. A `long` lets
   you add them silently. `Money` must refuse.
3. **Immutable.** Every operation returns a new instance, so two references can never surprise
   each other. That makes it free to share across threads and safe as a map key.

The interesting method is `allocate`. Splitting 100 pence three ways is not 33.33 each - it is
34, 33, 33. Divide, then hand the remainder out one minor unit at a time. Get it wrong and
invoices are off by a penny, and "where did the penny go" is a genuinely painful thing to chase
through a ledger.

Then the part that actually breaks in real code: **immutability at the boundary**. A record is
immutable, but a class holding a *collection* of them is only immutable if it guards both doors.
Copy on the way in, or the caller keeps a live reference to your internals and can mutate you
from the outside with no method call you could ever log. Return a view or a copy on the way out,
or you have handed your internals to whoever asked. Miss either and you have a mutable object
that *looks* immutable - which is worse than an obviously mutable one, because everyone will
reason about it as though it were safe.

**The trade-off:** defensive copying costs an allocation per call. For a small basket, nothing.
For a hot path over a large collection, measure it - and reach for a genuinely persistent data
structure rather than abandoning the guarantee.

## Build (25 min)

In `src/main/java/sd/p02/day16/`:

1. `Money` - `of`, `plus`, `minus`, `times`, `isNegative`, and `allocate`. Currency mismatch
   throws `IllegalArgumentException` naming both currencies.
2. `Basket` - defensive copy in the constructor, unmodifiable view from `items()`, and `total()`.

## Reflect (10 min)

1. Write the `allocate` invariant in one sentence. Why must it hold for every input?
2. `Basket` copies on the way in. What would the bug look like in production if it did not -
   and how would it show up in a log?
3. When is a `long` of pence genuinely the right choice over a `Money` type? (There are real
   cases.)

**Interview angle:** in any design touching money, saying "amounts are minor-unit integers with
currency attached, never floating point" early costs three seconds and signals that you have
shipped financial code.

## Stretch

Add `Money.percentage(int basisPoints)` and use it to compute VAT. Notice that rounding is now a
decision you have to make explicitly - and that being forced to make it explicitly is the point.

## Checkpoint

```powershell
.\day.cmd 16
```
