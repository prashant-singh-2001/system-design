# Day 13 - Liskov Substitution

**Phase 2 - SOLID** | 45 minutes

## Concept (10 min)

> Objects of a supertype should be replaceable with objects of a subtype without breaking the
> program.

The compiler enforces the SHAPE of an interface. It cannot enforce the BEHAVIOUR. Liskov is
about the half the compiler cannot see, and that is why it is the principle most often violated
without anybody noticing.

The rule in one line: **a subtype may promise more than the supertype. It may never promise
less.** Weakening a postcondition, strengthening a precondition, or throwing where the supertype
said it would succeed - all three break substitutability, and all three compile perfectly.

Today's example is `BoundedStore`, and it is a realistic one. Being bounded is fine and
necessary; an unbounded cache is an outage waiting to happen. The violation is what it does when
full: it silently ignores the write. `put` returns normally. No exception, no return value, no
log. The caller has every reason to believe the value is stored. It is not.

The tool that catches this is a **contract test**: the interface's guarantees written once as
executable tests, and inherited by every implementation. You write it once and every present and
future implementation is held to the same standard. It is the only practical enforcement
mechanism there is.

Note what the contract deliberately does *not* promise: that an old key stays retrievable
forever. Eviction is allowed. Accepting a write and not having it is not. Writing the contract
carefully is most of the work - a vague contract lets every implementation invent its own rules,
which is how you got here.

**The trade-off:** a precise contract constrains implementers. Say too much and you rule out
legitimate implementations (a contract demanding "all keys retained forever" would ban bounded
stores entirely). Say too little and substitutability means nothing. Getting that line right is
genuine design work.

## Build (25 min)

In `src/main/java/sd/p02/day13/`, fix `BoundedStore` so it honours the contract. Evict the
oldest entry to make room, then store the new one. `LinkedHashMap` gives you the oldest key
cheaply.

`KeyValueStoreContract` and both test subclasses are given - look at how little code it took to
hold a second implementation to the same standard.

## Reflect (10 min)

1. `BoundedStore` compiled, had the right types, and lost data. What would have caught it before
   production, other than the contract test?
2. Rewrite the interface's contract to permit "reject the write when full" as legitimate
   behaviour. What would `put`'s signature have to become? Which design would you prefer, and
   why?
3. `Optional<String> get(String)` returning `null` instead of `Optional.empty()` - is that a
   Liskov violation? Argue it.

**Interview angle:** the classic square/rectangle example is fine but abstract. A far better
answer is: "our read-through cache implemented the repository interface but returned stale data,
so callers that relied on read-your-writes broke. We wrote a contract test for the interface and
ran it against every implementation." That is Liskov as an operational concern, which is what it
actually is.

## Stretch

Write a contract test for `java.util.List` and run it against `ArrayList` and
`Collections.unmodifiableList(...)`. Watch it fail, then work out whether the JDK is wrong or
your contract is.

## Checkpoint

```powershell
.\day.cmd 13
```
