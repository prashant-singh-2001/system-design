# Day 85 - Design: chat system

**Phase 9 - HLD and capstone** | 45 minutes

## Concept (10 min)

Chat is the first design in this phase where **the connection itself is state**, and that changes
everything you have assumed so far.

HTTP is request-response and stateless, which is what let you make services interchangeable on Day
58. A WebSocket is a long-lived connection pinned to one server. So "deliver this message to Bob"
first requires knowing **which of your fifty servers Bob is connected to** - and that mapping
changes constantly.

Hence a **presence registry**: a shared, fast, expiring map from user to server. Redis, typically,
because the access pattern is exactly a hash with a TTL (Day 51).

The TTL is doing real work. A server that crashes cannot deregister its users, so entries must
expire on their own and clients refresh with a heartbeat. **Presence is therefore always slightly
wrong**, and the design has to tolerate that: a message routed to a server that no longer holds
the user must fall back to offline delivery rather than vanish.

Three delivery outcomes, and a design that only handles the first is a broadcast, not a chat
system:

- **Delivered** - recipient online, route to their server.
- **Queued** - recipient offline, store for reconnect. This is what makes chat feel reliable, and
  it is where most of the storage goes.
- **Fanned out** - a group message to each online member, queued for the rest. A 500-member group
  turns one send into 500 deliveries: Day 84's fanout problem in a different costume.

Then the hard part, which is where your deep dive should go: **ordering and delivery guarantees**.
Messages must appear in a consistent order for everyone in a conversation - which is Day 64's
partition key, with `conversationId` as the obvious choice. And delivery receipts (sent, delivered,
read) are three separate acknowledgements, each of which can be lost, so each needs the
at-least-once-plus-idempotency treatment from Phase 7.

## Build (25 min)

**First (about 10 min)** implement `PresenceRegistry` and `MessageRouter` in
`src/main/java/sd/p09/day85/`.

**Then (about 15 min)** write `designs/chat.md`. Assume 50M DAU, 40 messages per user per day,
group chats up to 500 members, and a requirement that messages are never lost.

## Reflect (10 min)

1. A server holding 50,000 connections dies. Walk through what every one of those users
   experiences, second by second.
2. Messages must be ordered within a conversation. What do you key by, and what does that make
   impossible?
3. Read receipts triple your write volume. Is that worth it? What would you do at 10x scale?

**Interview angle:** "WebSockets with a Redis presence registry keyed by user, TTL-refreshed by
heartbeat, and offline messages queued for delivery on reconnect" covers the architecture. Adding
"presence is always slightly stale, so an undeliverable route falls back to the offline queue"
shows you have thought about the failure rather than the demo.

## Stretch

Design end-to-end encryption. Notice how much of your architecture it invalidates: no server-side
search, no server-side ranking, and multi-device becomes a genuinely hard key-distribution problem.
That is worth feeling, because it is the trade Signal and WhatsApp actually made.

## Checkpoint

```powershell
.\day.cmd 85
```
