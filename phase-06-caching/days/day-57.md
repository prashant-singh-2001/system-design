# Day 57 - Load balancing

**Phase 6 - Caching** | 45 minutes

## Concept (10 min)

Four strategies, and the interesting part is what each one assumes.

**Round robin** - strict rotation. Assumes every request costs the same and every backend is
equally fast. Both are usually close enough to true, which is why it survives so well as the
default. Where it fails is long-lived connections: rotating *new* connections evenly says nothing
about how many are still open.

**Weighted round robin** - for a fleet that is not uniform, typically after a hardware refresh.
Weight is just "how many slots do you own", which is the same mechanism as yesterday's virtual
nodes.

**Least connections** - send work to whoever is least busy. This is the one that actually
*adapts*. Round robin is blind: if a backend is stuck on slow requests it keeps getting work at
the same rate. Least connections notices, because a slow backend accumulates open connections,
and steers away without anybody configuring anything. Its cost is per-backend state, which is
easy in one process and genuinely hard across a fleet of balancers that each see only their own
share of the traffic.

**Hash routing** - the same client always reaches the same backend, so that backend's local cache
gets warm. This is nginx's `hash $remote_addr`, and it is how you route cache keys to cache
shards.

And hash routing has a weakness you can now name precisely: **it is modulo hashing again.** One
backend going unhealthy changes the divisor and remaps almost every client - including clients
that had nothing to do with the failed backend. For sticky sessions that logs everybody out; for
a cache tier it cold-starts the lot. The production answer is to hash onto yesterday's *ring*,
which is exactly why yesterday came first. The final test measures this.

**The trade-off across all four:** the more a strategy adapts, the more state it needs, and state
in a load balancer is state you have to replicate, agree on, and keep correct during failures.

## Build (25 min)

Implement the four balancers in `src/main/java/sd/p06/day57/`. Every one must skip unhealthy
backends and throw `IllegalStateException` when none are healthy - silently returning a dead
backend turns an outage into a mystery.

Two details: use `Math.floorMod` on the rotation counter (a plain `%` on an int that eventually
overflows to negative will throw, months after deployment), and **rebuild the weighted expansion
on every call** rather than caching it, or a dead backend keeps getting traffic.

## Reflect (10 min)

1. One backend is at 100% CPU and answering slowly. Trace what round robin does, then least
   connections.
2. You run six load balancer instances. What breaks about least connections, and how would you
   approximate it? (Look up "power of two choices" - the answer is elegant.)
3. The final test showed hash routing remapping unrelated clients. Sketch the fix using
   yesterday's ring.

**Interview angle:** "round robin by default; least connections when request cost varies or
connections are long-lived; consistent hashing when I need cache locality" is three sentences
that cover almost every real case, and each names its condition.

## Stretch

Nginx is already wired up in `infra/`. Start three instances of Day 8's `TinyHttpServer` on ports
8081-8083, run `docker compose -f infra/docker-compose.yml up -d nginx`, and hit
`http://localhost:8080`. Watch the access log show the distribution, then uncomment `least_conn`
in `infra/nginx/nginx.conf` and watch it change.

## Checkpoint

```powershell
.\day.cmd 57
```
