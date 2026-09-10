# Day 67 - Dead letters and poison messages

**Phase 7 - Messaging** | 45 minutes

No Docker today - this is pure Java.

## Concept (10 min)

A **poison message** is one that fails every time: malformed JSON, a reference to a deleted row, a
bug triggered by one specific input. With at-least-once delivery and unbounded retries, that
message is redelivered forever.

And it does not fail alone. Day 64 told you a partition is handled by one consumer in strict
order, so a poison message **blocks everything behind it**. Consumer lag climbs, alerts fire, and
the cause is one row nobody can see. This is one of the most common and most confusing production
incidents in event-driven systems.

The remedy has two halves and you need both:

- **Bounded retries.** Try a few times - transient failures are real and usually resolve - then
  stop. Unbounded retries on a permanently broken message is just an infinite loop with extra
  steps.
- **A dead-letter queue.** Move the exhausted message aside so the pipeline continues, and keep it
  for a human.

The operational rule worth carrying out of today:

> **A DLQ with no alert on it is a silent data-loss mechanism.**

Messages go there and nobody looks. Alert on depth, always. And a DLQ is an **inbox, not a bin** -
it needs a replay path, or you have merely relocated the loss.

**The trade-off:** dead-lettering trades strict completeness for availability. You are choosing to
let the pipeline keep moving while a few messages wait for attention. That is nearly always
right - and it is a choice, which someone has to own.

## Build (25 min)

In `src/main/java/sd/p07/day67/RetryingConsumer.java`:

1. `consume` - attempt each message up to `maxAttempts` **total** attempts; on exhaustion, add it
   to the DLQ with its attempt count and **carry on to the next message**. One bad message must
   never stop the batch.
2. `replayDeadLetters` - re-run the DLQ through the handler, **clearing the queue first** so
   anything still failing is re-added rather than duplicated. Get that ordering wrong and the DLQ
   doubles on every replay, which is a memorable way to learn it.

## Reflect (10 min)

1. Why is the attempt count recorded on the dead-lettered message? What does an operator do with
   it?
2. Retrying a message immediately, three times, in a tight loop - when is that wrong? What would
   you add? (Day 72 has the answer.)
3. Your DLQ has 40,000 messages from a bad deploy. Walk through your recovery, including how you
   avoid a second incident during replay.

**Interview angle:** "bounded retries with exponential backoff, then a dead-letter topic that we
alert on and can replay" is a complete failure-handling story. Most answers stop at "we retry".

## Stretch

Add exponential backoff between attempts, and a separate retry topic per delay tier - the pattern
Uber published. It keeps slow retries from blocking the main partition, which is the flaw in
in-place retrying that today's simple version still has.

## Checkpoint

```powershell
.\day.cmd 67
```
