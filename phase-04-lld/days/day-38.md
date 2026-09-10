# Day 38 - Extensible multi-channel dispatch with retries

**Phase 4 - Low-level design craft** | 45 minutes

## Concept (10 min)

A notification service is where two ideas you have already built once each get combined on a
genuinely new domain: **Strategy + Factory's extensibility** (Day 21, Day 12) and **Decorator's
retry logic** (Day 24). Today's `NotificationDispatcher` inlines a retry loop directly rather
than wrapping a separate `RetryDecorator`, because the exercise is about the DISPATCHER's
design, not about re-importing a class from three phases ago - but you should recognise the
shape the moment you see `dispatch`'s retry loop.

The one design decision that matters most: `Notification.channel()` is a plain `String`, not an
enum. An enum is CLOSED - adding `"WEBHOOK"` as a fourth channel means editing the enum
declaration, which means editing (or at least recompiling) everything that switches over it. A
string key into a `Map<String, NotificationSender>` is OPEN - a new channel is one more map
entry, registered wherever the dispatcher is constructed, with zero changes to
`NotificationDispatcher` itself. This is the exact same "open for extension, closed to
modification" property Day 12's shipping calculator and Day 21's hash-function factory both had,
now applied to a registry keyed by string instead of by carrier name or algorithm name.

## Build (25 min)

Implement `NotificationDispatcher` in `src/main/java/sd/p04/day38/`: look up the sender for the
notification's channel (or fail loudly, naming the channel, if none is registered); retry that
one send up to `maxAttempts` total attempts on `NotificationException`; rethrow the last failure
if every attempt is exhausted.

`EmailSender`, `SmsSender`, `PushSender` (always succeed) and `FlakySender` (configurable
failures, for exercising retries) are given.

## Reflect (10 min)

1. `newChannelRequiresNoDispatcherChanges` registers a `"WEBHOOK"` channel as a one-line lambda
   with zero changes to `NotificationDispatcher.java`. What would have had to change if
   `Notification.channel()` were an enum instead of a `String`?
2. Each channel gets its OWN independent retry budget, because each `dispatch` call resolves and
   retries exactly one sender. What would change if you instead wanted a SHARED retry budget
   across all channels for one logical notification - "try email, and if that's exhausted, fall
   back to SMS"? Sketch the shape of that method, briefly.
3. `NotificationException` is unchecked. What would you lose, and what would you gain, if it
   were a checked exception instead - and does your answer change depending on whether
   `NotificationSender` is a `@FunctionalInterface` meant to be implemented as a lambda?

**Interview angle:** "we'd have an interface with implementations per channel" is the correct
starting sentence and an incomplete answer on its own. The stronger version names the SPECIFIC
mechanism that keeps it extensible - a string- or class-keyed registry, not a closed enum or
`if/else` chain - because that is the detail that actually gets tested when the interviewer says
"now add a fourth channel" as a follow-up.

## Stretch

Add a `DeadLetterNotificationDispatcher` decorator - Day 24's shape again - that wraps
`NotificationDispatcher` and records any notification that exhausts its retries into a list
instead of letting the exception propagate. You are building a miniature version of Day 67's
dead-letter queue.

## Checkpoint

```powershell
.\day.cmd 38
```
