# Phase 2 - Design principles: SOLID and clean architecture

**Days 11-20**

## Why this phase comes second

Phase 1 was about the machine. This phase is about the code you put on it.

SOLID is usually taught as five slogans and a set of contrived shape hierarchies, which is why
most people can recite it and few can apply it. Here every principle arrives the same way: a
piece of deliberately bad code, a test suite that must keep passing, and a refactor. You feel
what the principle buys, which is the only way to learn when to ignore it - and you should
sometimes ignore it.

The reason this comes before any distributed-systems material is that a service you cannot
change safely cannot be scaled safely either. Every technique in Phases 5-8 - swapping a
storage engine, adding a cache, putting a queue in the middle - is only cheap if the seams are
already in the right places. SOLID is how you get the seams.

## What you will be able to do afterwards

- Look at a class and say how many independent forces can demand a change to it
- Add a feature by writing a new class rather than editing an old one, and know when that is
  over-engineering
- Write a contract test that holds every implementation of an interface to the same standard
- Push infrastructure to the edges so business logic can be tested in microseconds
- Refactor unfamiliar code safely, behind characterization tests

## The through-line

Days 11-15 are the **five principles**, one per day, each as a refactor.
Days 16-19 are the **techniques they enable**: value objects, hexagonal architecture,
aggregates, errors as values.
Day 20 is a capstone kata that needs all of them at once.

## Two honest caveats

**Every principle has a cost.** OCP spreads logic across files. Hexagonal architecture adds
indirection. Result types are viral. The briefs name these costs explicitly, because an engineer
who can only argue one side of a trade-off is not yet making a decision - they are following a
rule.

**Apply them at the axis of change you actually expect.** Making everything extensible in every
direction is not clean code, it is a different kind of mess. The skill is guessing correctly
about what will change, and that guess comes from the domain, not from the principles.

## The one idea to carry forward

**Push the decisions you cannot test to the edges.** Clock, database, network, randomness -
inject them all. The core then becomes pure logic you can test at a million cases per second,
and every phase after this one depends on your having done it.
