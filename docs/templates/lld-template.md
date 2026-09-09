# LLD: <system>

Budget: 45 minutes. Timings are the point - practice them.

## 1. Requirements (5 min)

**Functional** - what it must do. Number them; you will refer back.
1.

**Non-functional** - scale, latency, concurrency, durability.
-

**Explicitly out of scope** - say these out loud. Scoping is a senior signal.
-

## 2. Core entities (5 min)

Nouns first. For each: what it owns, what invariants it protects.

| Entity | Responsibility | Invariants |
|---|---|---|

## 3. Public API (10 min)

Method signatures only. Get these right and the classes almost write themselves.

```java
```

## 4. Class design (10 min)

Relationships, and which patterns you are reaching for and why.
Name the extension point: where will the next requirement land?

## 5. Concurrency (5 min)

What is shared? What is the locking strategy? What is the contention hot spot?
Answer this even for single-threaded designs - saying "single-threaded by design" is an answer.

## 6. Tests (5 min)

The three tests that would catch the bugs you are most worried about.
1.

## 7. Trade-offs and extensions (5 min)

What you deliberately did not build, and what would change if requirement X arrived.
