# Day 11 - Single Responsibility

**Phase 2 - SOLID** | 45 minutes

## Concept (10 min)

The Single Responsibility Principle is usually stated as "a class should do one thing", which is
useless because "one thing" is undefined. The precise version is Robert Martin's:

> A class should have one, and only one, reason to change.

"Reason" means a person or a team who can demand a change. Look at `LegacyOrderProcessor` and
count them: the rules team owns validation, finance owns tax, the platform team owns storage,
and marketing owns the confirmation email. Four groups, one file, guaranteed merge conflicts and
a change from any one of them risking all four.

There is a much more practical tell, and it is the one to internalise:

**If testing one rule requires standing up unrelated infrastructure, responsibilities are
tangled.** You cannot test the tax calculation in that class without a database and an email
server. That is the smell. Not line count, not method count - the shape of what a test has to
set up.

**The trade-off:** splitting means more files and more indirection. Reading the whole workflow
now means opening five files instead of one. That is a real cost, and it is why SRP taken to an
extreme produces codebases where nothing is anywhere. Split along the lines where change
actually arrives, not on principle.

## Build (25 min)

Read `LegacyOrderProcessor` first. Then in `src/main/java/sd/p02/day11/`:

1. `PricingService` - subtotal, discount, tax, shipping. Same arithmetic, copied exactly.
2. `OrderValidator` - the same rules with the same exception messages.
3. `OrderProcessor` - takes the validator, the pricing service, and the two ports as
   constructor parameters. Its `process` becomes four lines.

The two ports (`OrderRepository`, `ConfirmationSender`) are given. Keep the exception messages
identical - the final test asserts your total matches the legacy total, because a refactor
changes structure and never behaviour.

## Reflect (10 min)

1. How many lines of setup does the pricing test need now, versus what it would have needed
   against the legacy class?
2. The tax rate changes to 22%. Which files do you touch? Repeat the question for: switching to
   DynamoDB, and moving confirmations to SMS.
3. Is `OrderProcessor` itself a violation? It calls four collaborators. What is its single
   reason to change?

**Interview angle:** when reviewing a design, "what would have to change for this class to
change?" is a sharper question than "does this class do one thing?" - and it produces an answer
you can act on.

## Stretch

The legacy class also writes an audit log. Where should that go? Argue for putting it in the
processor, then argue for a decorator (you will build exactly that on Day 24), and pick.

## Checkpoint

```powershell
.\day.cmd 11
```
