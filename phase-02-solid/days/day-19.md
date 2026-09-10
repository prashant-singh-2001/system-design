# Day 19 - Errors as values

**Phase 2 - SOLID** | 45 minutes

## Concept (10 min)

Exceptions are excellent for the genuinely exceptional - a disk failing, a bug, a violated
invariant. They are a poor fit for outcomes you fully expect: a malformed email, a declined
card, a username already taken. Three reasons:

- **They are invisible in the signature.** `User parse(String)` does not tell you it can fail.
  `Result<User, String>` does, and the compiler makes you deal with it.
- **They stop at the first problem.** A form with three bad fields should report three errors,
  not throw on the first. Values accumulate; throws do not. This is the case exceptions
  structurally cannot serve - a user fixes one field, resubmits, discovers the next, three round
  trips for one form.
- **They are expensive and they jump.** Filling in a stack trace costs real time on a hot path,
  and a throw transfers control somewhere you cannot see from the call site.

The tool is a **sealed** interface with two cases. Sealed means the compiler knows the complete
set, so a switch over a `Result` needs no default branch and will fail to compile if a third
case is ever added. That is the type system doing work that used to require discipline.

`flatMap` is what makes it usable: it chains operations that can themselves fail, and the first
failure short-circuits everything after it. You get the readability of the happy path with none
of the invisible control flow.

**The trade-off, and it is a real commitment:** `Result` is viral. Once a method returns one, its
callers must handle it, and the style spreads outward through the codebase. That is why the
usual advice is to use it for *expected domain outcomes* and keep exceptions for genuine faults -
and to convert at the boundary, which is what `orElseThrow` is for. A codebase that half-adopts
this is worse than one that picks either side.

## Build (25 min)

In `src/main/java/sd/p02/day19/`:

1. `Result<T, E>` - the two records, factories, `isSuccess`, `map`, `flatMap`, `orElse`,
   `orElseThrow`.
2. `SignupValidator` - collect **every** failure in the listed order, not just the first.

## Reflect (10 min)

1. Write the signature of a method that can fail, three ways: throwing, returning `Optional`,
   returning `Result`. What does each communicate to the caller?
2. Where in a web service would you convert a `Result` back into an exception, and why there?
3. The validator accumulates errors. Why can `flatMap` not do that, and what would you need
   instead? (This is the difference between a monad and an applicative, if you want the name.)

**Interview angle:** "expected failures are return values, unexpected failures are exceptions" is
a clear position to hold in a design review - as long as you can also say where you convert
between them.

## Stretch

Add `Result.combine(List<Result<T, E>>)` that accumulates all errors or returns all values. That
is the missing piece from question 3, and writing it will make the distinction concrete.

## Checkpoint

```powershell
.\day.cmd 19
```
