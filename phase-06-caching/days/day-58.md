# Day 58 - Stateless services

**Phase 6 - Caching** | 45 minutes

Start Docker Desktop - this one uses a real Redis.

## Concept (10 min)

Session state in the application process is the default in every framework, and it is the single
decision that makes your instances non-interchangeable.

Instance A knows about a session that B and C have never heard of, so the load balancer must keep
sending that user back to A forever. That one constraint causes a cascade:

- **Deploys log people out.** Restarting A destroys its sessions.
- **Autoscaling is lopsided.** New instances get only new users, so scaling out under load does
  not relieve the instance that is actually overloaded.
- **Losing an instance loses its users' state**, not just its capacity.
- **Load balancing degrades.** Yesterday's least-connections strategy cannot move a pinned
  client, so your adaptive balancer stops applying to exactly the traffic that needs it.

None of these is a bug. `InMemorySessionStore` is correct code. The problem is architectural:
**state in the instance is what makes the instance special.**

Move it out - to Redis, to a database, or into a signed token the client carries - and every
instance becomes interchangeable. Deploys stop logging people out, autoscaling works, and the
balancer is free again. It is a small change with a disproportionate effect, and it is the
precondition for most of what "horizontal scaling" means.

**The trade-off:** every session read is now a network round trip - nanoseconds become
milliseconds. Almost always worth it. Where it is not, the answer is a short-TTL local cache in
front of the shared store, which puts you straight back into Day 55's invalidation problem.
There is no free lunch here, only a better-understood bill.

## Build (25 min)

Implement `RedisSessionStore` in `src/main/java/sd/p06/day58/`. Store each session as a HASH
under `session:<id>` - Day 51's choice for Day 51's reason - with a `userId` field and one
`attr:`-prefixed field per attribute.

Two things the tests check:

- **Set a TTL on save.** A session store with no expiry is a memory leak with a login page.
- **`hgetall` on a missing key returns an empty map, not null.** Check for emptiness. Getting
  this wrong gives you either a NullPointerException or a silent empty session, depending on
  which way you guess.

## Reflect (10 min)

1. List everything that breaks when instance A restarts, under each of the two stores.
2. The alternative to a shared store is a signed token (a JWT) that the client carries. What does
   that make easy, and what does it make genuinely hard? (Think about logout.)
3. Session reads are now a network hop. When would you put a local cache in front, and what
   would you have to build alongside it?

**Interview angle:** "the services are stateless - sessions live in Redis - so any instance can
serve any request and we can deploy and autoscale freely" is worth saying early in any design.
It removes a whole category of follow-up questions about stickiness.

## Stretch

Implement the JWT alternative: sign the session into a token the client holds, so there is no
server-side lookup at all. Then implement logout, and discover why token revocation is the hard
part - and why most systems end up keeping a small server-side deny-list anyway.

## Checkpoint

```powershell
.\day.cmd 58
```
