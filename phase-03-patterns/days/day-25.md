# Day 25 - Adapter + Facade: anti-corruption layers

**Phase 3 - Patterns** | 45 minutes

## Concept (10 min)

`LegacyPaymentGatewayApi` is what a real third-party payment SDK usually looks like: amounts as
`double` DOLLARS (the exact anti-pattern Day 16 spent a whole day warning about, now arriving
from OUTSIDE your control), an expiry packed into one `"MMYY"` string, success or failure as a
bare status CODE, and the actual error detail left in mutable state you have to remember to go
fetch separately after a failed call. You cannot fix the vendor's API. What you can do is build
exactly one **Adapter** that absorbs every one of those smells, so nothing else in the codebase
ever touches a double, a two-digit year, or a status code again. If the vendor changes their SDK
next year, this is the only file that moves - that containment is the entire argument for the
pattern.

**Facade** solves an adjacent but different problem: sequencing several ALREADY-REASONABLE
subsystems - a fraud check, the payment processor, a receipt service - behind the one call a
caller actually wants, `purchase(orderId, request)`. An adapter translates one hostile interface;
a facade coordinates several cooperating ones. You are using both today because that is exactly
how they show up together in a real checkout path: the adapter sits at the boundary with the
outside world, and the facade sits at the boundary with your own domain's other subsystems.

## Build (25 min)

In `src/main/java/sd/p03/day25/`:

1. **`LegacyPaymentGatewayAdapter`** - convert cents to dollars, format the expiry as zero-padded
   `MMYY`, call the legacy API, and map its status code (`0`/`1`/`2`) to
   `Approved`/`Declined`/`GatewayError`.
2. **`PaymentFacade`** - if `fraudChecker.isSuspicious(request)`, return
   `Declined("blocked for fraud review")` WITHOUT calling the processor at all; otherwise charge,
   record a `Receipt` on approval, and return the result.

## Reflect (10 min)

1. `LegacyPaymentGatewayApi` is a fake you can fully script (status, transaction id, error
   message) rather than a simulation of real gateway logic. Why does that make it a BETTER test
   double for exercising the adapter's translation logic than a more "realistic" fake would be?
2. The facade blocks a suspicious payment before the processor is ever called - verified by a
   test that counts calls to a wrapped processor. Why does "never call the gateway" matter here
   beyond just "return the right answer" - what would calling it anyway cost in a system with a
   real payment processor charging a per-attempt fee?
3. Name a hostile third-party API you have actually worked with (a shipping carrier, an SMS
   provider, an internal legacy system - anything). What specifically would your adapter need to
   absorb, using the same four-smell list from today's concept section as a checklist?

**Interview angle:** "we'd wrap it in an adapter" is the right instinct stated too vaguely. The
strong answer names WHICH specific smells the adapter absorbs - unit mismatches, a
stateful/sequential API, an untyped result - and says precisely where the boundary sits: one
adapter, one hostile dependency, nothing upstream of it ever seeing the vendor's shape again.

## Stretch

Add a second `PaymentProcessor` implementation - a `StripeLikeAdapter` sketch (comments are fine)
wrapping a DIFFERENT, more modern hostile shape (say, a JSON-over-HTTP client returning a
`CompletableFuture`) - and confirm `PaymentFacade` needs zero changes to accept it. That is DIP
(Day 15) and Adapter working together.

## Checkpoint

```powershell
.\day.cmd 25
```
