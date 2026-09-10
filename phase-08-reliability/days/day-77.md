# Day 77 - Distributed locks and fencing tokens

**Phase 8 - Reliability** | 45 minutes

## Concept (10 min)

A distributed lock cannot be held indefinitely - the holder might die and never release it. So
locks are **leases**: they expire. And that is exactly where the danger enters.

The failure, in order:

1. Client A acquires a 30-second lease.
2. Client A stops the world - a long GC pause, a hypervisor stall, a network partition. It is not
   dead, just frozen, and it has **no idea any time has passed**.
3. The lease expires. Client B legitimately acquires it.
4. Client A resumes, still believing it holds the lock, and writes.

Two clients now believe they hold the same lock, and **no amount of care in the lock service
prevents it**. Redlock's much-discussed weakness is precisely this. The problem is not the lock -
it is the gap between checking and acting, and nothing on the lock service's side can observe that
gap.

The fix is a **fencing token**: a number that increases with every acquisition. The client passes
it to the resource, and the resource refuses any token lower than the highest it has seen. Client A
wakes up holding token 33, the storage has already accepted 34, and A's write is rejected.

Note where the enforcement lives: **at the resource**, not the lock service. The lock service
cannot know a client froze. The resource sees the writes and can order them, so it is the only
component that can make the guarantee. Real systems do this - HBase and ZooKeeper designs pass
epoch numbers, and object stores offer conditional writes for the same reason.

The practical conclusion: **"we use a distributed lock" is not by itself a correctness argument.**
The question that follows is "and what fences the resource?"

There is also a simpler bug the tests cover: releasing a lock you no longer own. Your lease
expired, someone else took it, and your cleanup frees *theirs* - while they are mid-write.

## Build (25 min)

In `src/main/java/sd/p08/day77/`:

1. `DistributedLock` - `tryAcquire` (grant if free or expired, issue the next token), `release`
   (owner only), `isHeldBy`. Driven by an injected `Clock`.
2. `FencedResource.write` - accept only tokens at or above the high-water mark; count rejections.

## Reflect (10 min)

1. Walk through the zombie-writer test and name the exact moment where the lock service could have
   helped. (There isn't one - that is the point.)
2. Where would you get monotonic tokens in production? Name two sources and their failure modes.
3. Your resource is S3. What is the fencing mechanism there? (Look up conditional writes.)

**Interview angle:** "a distributed lock plus fencing tokens, because a GC pause can make a lock
holder act after its lease expired and only the resource can reject that write" is a genuinely
distinguishing answer - most candidates stop at the lock.

## Stretch

Read Martin Kleppmann's "How to do distributed locking" and Antirez's reply. It is the clearest
public disagreement in distributed systems, and you now have the code to follow both sides.

## Checkpoint

```powershell
.\day.cmd 77
```
