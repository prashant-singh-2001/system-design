# Day 12 - Open/Closed

**Phase 2 - SOLID** | 45 minutes

## Concept (10 min)

> Software entities should be open for extension, but closed for modification.

The test is concrete and unsentimental: **can you add a case without editing the existing file?**

`LegacyShippingCalculator` fails it. Every new carrier means editing that method, which means
re-reviewing it, re-testing every existing carrier, and redeploying the module that owns it. A
partner integration that ought to be purely additive becomes a change to shared, working code.
Three teams adding three carriers all edit the same twenty lines, and the file becomes a
merge-conflict magnet.

The fix is a **strategy registry**: an interface for the varying behaviour, one small class per
case, and a map from key to strategy. Adding a carrier becomes: write a class, register it.

There is a second, less obvious win. The enumeration of carriers moves from a hard-coded chain
into runtime configuration, which is what makes per-tenant and per-region carrier sets possible
at all. Extensibility in the code turned into a product capability.

**The trade-off, and it is real:** the logic is now spread across five files instead of one, and
you cannot see every carrier's rules on a single screen. Debugging means following a map lookup
into a class you have to find. OCP is worth paying for at the axis of change you actually
expect - here, new carriers, which certainly will arrive - and is over-engineering everywhere
else. A branch on a two-case enum that has been stable for five years should stay a branch.

## Build (25 min)

In `src/main/java/sd/p02/day12/`:

1. `ShippingStrategy` - one method, `long quoteCents(Shipment)`. Keep it to one method so that
   a lambda can implement it; the last test depends on that.
2. `RoyalMailStrategy`, `DhlStrategy`, `PickupStrategy` - move each branch across unchanged.
3. `ShippingCalculator` - a registry with `register(...)`, a `quoteCents(...)` that delegates,
   and a static `withDefaults()`. Unknown carriers still throw with the message
   `unknown carrier: X`.

The key test registers a `DRONE` carrier that did not exist when `ShippingCalculator` was
written. If it passes, the class is genuinely closed to modification.

## Reflect (10 min)

1. Which files did you have to touch to add `DRONE`? Which would you have touched in the legacy
   design?
2. Name a case in your own work where an if/else chain is *correct* and a strategy registry
   would be over-engineering. What distinguishes it from this one?
3. Where does the registry get populated in a real application? What are the trade-offs between
   hard-coded defaults, a config file, and classpath scanning?

**Interview angle:** the useful phrasing is about the axis of change: "I expect new carriers, so
I would make carrier a strategy. I do not expect new order states, so those stay an enum with a
switch." That shows you are choosing rather than applying a rule uniformly.

## Stretch

Add a `CompositeStrategy` that quotes several carriers and returns the cheapest. Notice that it
implements the same interface it consumes - that is the Composite pattern, and it costs you
nothing extra now that the seam exists.

## Checkpoint

```powershell
.\day.cmd 12
```
