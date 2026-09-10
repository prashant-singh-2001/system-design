# Day 14 - Interface Segregation

**Phase 2 - SOLID** | 45 minutes

## Concept (10 min)

> No client should be forced to depend on methods it does not use.

`LegacyUserRepository` has twelve methods serving three completely different audiences: request
handlers that only read, the signup flow that writes, and a nightly maintenance job. Every
consumer depends on all twelve.

The concrete damage:

- A read-only cache must implement `vacuum()`. It has nothing sensible to put there, so it
  throws `UnsupportedOperationException` - and you have just built yesterday's Liskov violation
  as a direct consequence. **ISP violations breed LSP violations.**
- A test double for a read path has to stub twelve methods to exercise one.
- Adding an admin method recompiles and re-tests every consumer, including those that will never
  call it.
- You cannot express "this component may read but must not write" in the type system, so that
  rule lives only in code review and gets forgotten.

That last one is the real prize. After the split, "read-only" stops being a convention and
becomes a compile-time guarantee. Nobody can accidentally write from a component that only holds
a `UserReader` - not because they were careful, but because there is no method to call.

Segregate by **role** - who needs this set of operations - not by data type. The question is not
"what can a user repository do", it is "what does each caller actually need". Getting this
backwards produces `UserReadInterface` and `UserWriteInterface` split down technical lines that
nobody actually consumes.

**The trade-off:** more interfaces to name, and implementations that legitimately do everything
now list three names in their `implements` clause. Modest costs, but the failure mode is real -
segregate too finely and you get a dozen single-method interfaces nobody can keep straight.

## Build (25 min)

In `src/main/java/sd/p02/day14/`, fill in the three role interfaces (`UserReader`, `UserWriter`,
`UserAdmin`) with the exact method signatures listed in their javadoc, and make `UserRepository`
extend all three while declaring nothing of its own.

`ReadOnlyUserCache` is given and declares `implements UserReader`, so it will not compile until
`UserReader` has its four methods - a useful, immediate feedback loop.

The assertions are reflective on purpose. ISP is a structural principle, so the test is a
structural one: an architecture fitness function, the same idea you will use again on Day 17.

## Reflect (10 min)

1. Look at `ReadOnlyUserCache`. How many methods did it have to implement before the split, and
   how many now?
2. How does an ISP violation force an LSP violation? Trace the exact mechanism.
3. Your service needs to read users and send emails. Should it take a `UserReader` and an
   `EmailSender`, or one `UserService` facade? What decides?

**Interview angle:** "we split the repository by role, so the read path takes a `UserReader` and
literally cannot write - it stopped being a code-review rule and became a compile error." That
is a specific, checkable outcome, which is what makes it credible.

## Stretch

Look at `java.util.concurrent.ExecutorService`. It has methods for submitting work and methods
for lifecycle management. Is that an ISP violation? Argue both sides, then decide.

## Checkpoint

```powershell
.\day.cmd 14
```
