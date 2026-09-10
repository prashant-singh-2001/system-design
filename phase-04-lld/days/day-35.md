# Day 35 - Scheduling and dispatch under constraints

**Phase 4 - Low-level design craft** | 45 minutes

## Concept (10 min)

An elevator system is a scheduling problem wearing a physical costume: a fleet of workers (cars),
a stream of requests (hall calls and floor selections), and a dispatch rule deciding which worker
answers which request. The same shape reappears as load-balancer backend selection, task
assignment across worker nodes, and (again) container scheduling.

Today's elevator is simulated one FLOOR-STEP at a time, advanced by explicit `step()` calls,
rather than with real threads and real time. This is a deliberate simplification: the interesting
part of this problem is the SCHEDULING LOGIC (which floor to visit next, which elevator answers a
call), and simulating discretely makes that logic completely deterministic and fast to test - the
same reason Days 33, 39 and 40 all inject a controllable clock instead of using real time.

The dispatch strategy itself is Day 21 and Day 12's shape again: an interface
(`DispatchStrategy`) with one method, implementations swapped at runtime, a coordinator
(`ElevatorSystem`) that depends on the interface and never on a concrete strategy. Today's
`NearestElevatorDispatchStrategy` is deliberately the SIMPLEST defensible rule - nearest by floor
distance, ignoring current direction entirely - named as a simplification on purpose rather than
disguised as a complete answer.

## Build (25 min)

In `src/main/java/sd/p04/day35/`:

- **`Elevator.step()`** - arrive (clear a pending floor) if already there; otherwise move one
  floor toward the nearest pending request.
- **`NearestElevatorDispatchStrategy`** - the fleet member with minimum distance to the
  requested floor, ties broken by list order.
- **`ElevatorSystem`** - `requestPickup` delegates to the strategy then to the chosen elevator;
  `stepAll` advances the whole fleet by one step each.

## Reflect (10 min)

1. `NearestElevatorDispatchStrategy` ignores direction entirely. Describe a concrete scenario -
   floors and directions for two elevators and one request - where this produces a WORSE choice
   than a direction-aware strategy would, and say precisely what a direction-aware rule would
   check instead.
2. `Elevator.step()` checks "have I arrived" BEFORE checking "where do I move next", and treats
   arrival as its own step rather than folding it into a move. What test would fail, and how,
   if you swapped that order?
3. This simulation advances one elevator, one floor, at a time, driven by explicit calls rather
   than real threads. What would change about `ElevatorSystem`'s design if you needed each
   elevator to genuinely run on its own thread, moving in real time?

**Interview angle:** the interviewer is listening for whether dispatch and per-elevator movement
are cleanly SEPARATED - a coordinator that picks an elevator, and an elevator that decides its
own next move, each ignorant of the other's internals. Tangling those two responsibilities into
one big scheduling loop is the most common way this design goes wrong under time pressure.

## Stretch

Upgrade `NearestElevatorDispatchStrategy` into a `DirectionAwareDispatchStrategy`: prefer an
elevator already moving TOWARD the requested floor in the requested direction over an idle or
wrong-direction one, even if it is not the closest by raw distance. Write the test that would
have failed against yesterday's simpler strategy.

## Checkpoint

```powershell
.\day.cmd 35
```
