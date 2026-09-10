# Day 34 - Modelling physical resources and allocation

**Phase 4 - Low-level design craft** | 45 minutes

## Concept (10 min)

A parking lot is a small, concrete example of a genuinely recurring system design shape:
**a fixed pool of typed physical resources, allocated under a fit constraint, claimed
concurrently.** The same shape reappears as hotel room booking, seat reservation, and (with
"physical" replaced by "virtual") container scheduling onto worker nodes.

Two design decisions carry the whole exercise:

**Fit, not just availability.** A motorcycle can use any spot; a car needs at least a medium
one; a bus needs a large one. Getting the compatibility rule right is the actual modelling work
- and a good allocator does more than check fit, it PREFERS the smallest fitting spot, so your
one remaining large spot is still there when an actual bus needs it five minutes later. Handing
a motorcycle the last large spot because "it fits" is technically correct and operationally
wrong.

**Concurrent allocation must never double-assign.** Two threads racing for the last compatible
spot must never both walk away with a ticket for it. You do not need a lock over the whole lot
to guarantee this - a `ConcurrentHashMap` keyed by spot id, claimed with `putIfAbsent` (which
atomically fails if another thread just won that exact spot), gives you per-spot atomicity with
no contention between threads claiming DIFFERENT spots at the same time.

## Build (25 min)

In `src/main/java/sd/p04/day34/`, implement `ParkingLot`:

- `parkVehicle(vehicleId, type)` - find the smallest fitting, unclaimed spot; claim it
  atomically; issue a `Ticket`, or return `Optional.empty()` if nothing fits.
- `unparkVehicle(ticketId, exitAt)` - validate the ticket, price the stay via the injected
  `PricingStrategy`, free the spot, return the cost.
- `availableSpots(type)` - a count, for whoever is watching the lot fill up.

`HourlyPricingStrategy` (rounds every started hour up) is given.

## Reflect (10 min)

1. `motorcyclePrefersSmallestFittingSpot` checks the SMALL spot is chosen over the LARGE one.
   Describe, concretely, the failure this prevents: what happens later in the day if every
   allocation just grabbed the first fitting spot it found, regardless of size?
2. `putIfAbsent` on a `ConcurrentHashMap` is doing real work here - it is what makes "check if
   free, then claim it" atomic instead of two separate steps a second thread could interleave
   between. What specifically would go wrong with a plain `HashMap` and a `contains` check
   followed by a `put`?
3. A real parking garage also needs to handle a lost ticket, a validated (discounted) ticket, and
   reserved spots for permit holders. Pick one and sketch, in a sentence or two, which existing
   piece of your design it would extend and which it would require rebuilding.

**Interview angle:** "I'd use a map to track occupied spots" is table stakes. The signal is the
word ATOMIC showing up in your own explanation before the interviewer has to ask "but what if two
people try to park at the same time" - naming the race condition unprompted is what separates a
correct sequential design from a correct CONCURRENT one.

## Stretch

Add a `Reservation` concept: a spot can be held for a specific vehicle for N minutes before
being released back to the general pool if unclaimed. What new state does a spot need, and does
your existing `ConcurrentHashMap`-based claiming still work unchanged, or does it need to grow a
time dimension too?

## Checkpoint

```powershell
.\day.cmd 34
```
