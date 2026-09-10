# Day 23 - Observer: the seed of pub/sub

**Phase 3 - Patterns** | 45 minutes

## Concept (10 min)

**Observer**: a subject notifies a set of subscribers when something happens, without knowing
or caring who they are or how many there are. That is the entire idea behind every pub/sub
system you will ever use - an in-process `EventBus` today, Kafka starting Day 61 - the
difference between them is durability, delivery guarantees and network hops, not the shape.

Two design decisions in `EventBus` matter more than they look:

**Routing by exact type.** `subscribe(OrderPlaced.class, ...)` only ever hears about
`OrderPlaced` events, never `OrderCancelled` ones, even though both implement `Event`. This is
what lets a consumer declare "I care about this one thing" and have the compiler-adjacent type
system back that claim up, rather than every subscriber receiving everything and filtering it
themselves with an `instanceof` chain.

**Failure isolation.** If one subscriber throws, every subscriber registered AFTER it must still
be notified. Get this wrong - let one subscriber's exception unwind out of `publish()` - and you
have built a system where the order you happened to register two unrelated consumers in decides
whether the second one ever runs. A logging subscriber added last should never be able to
silently stop a billing subscriber added first from being notified, and neither should the
reverse.

## Build (25 min)

In `src/main/java/sd/p03/day23/`, implement `EventBus`:

- `subscribe(eventType, subscriber)` - register, return a `Subscription` whose `unsubscribe()`
  removes it.
- `publish(event)` - notify every current subscriber registered for `event.getClass()`, catching
  and swallowing any exception a subscriber throws so it cannot stop delivery to the rest.

A `Map<Class<?>, List<Subscriber<?>>>` (with the inevitable unchecked cast at the call site,
isolated to one line and commented) is the natural shape here.

## Reflect (10 min)

1. `aFailingSubscriberDoesNotBreakDelivery` catches a subscriber's exception and moves on
   silently. In a real system, silently swallowing an exception is usually itself a bug. What
   would you actually do with the exception in production - and why does that answer belong
   OUTSIDE today's `EventBus`, as a subscriber's own concern rather than the bus's?
2. Routing keys off `event.getClass()` exactly - a subscriber for a supertype or a shared
   interface gets nothing. Give one situation where that is exactly right, and one where you
   would want the looser behaviour instead.
3. This bus lives in one process, in one JVM, with no persistence - a publish that happens
   before any subscriber exists is simply lost. Name the one Kafka concept (Phase 7) that
   removes that limitation, and what it costs to get it.

**Interview angle:** the interviewer wants to hear you distinguish an in-process event bus from
a message queue, precisely: an event bus has no persistence, no delivery guarantee beyond "the
subscribers that exist right now, in this process, get called synchronously" - which is exactly
right for decoupling code within one service, and exactly wrong for anything that has to survive
a crash or cross a process boundary.

## Stretch

Add `publishAsync(event, Executor executor)`, dispatching each subscriber on the given executor
instead of the calling thread. Now ask: does failure isolation still work the same way, and what
new failure mode did you just introduce by making delivery concurrent?

## Checkpoint

```powershell
.\day.cmd 23
```
