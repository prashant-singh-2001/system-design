# Day 76 - Raft leader election

**Phase 8 - Reliability** | 45 minutes

## Concept (10 min)

Today you implement consensus. Not to memorise a protocol - to make it concrete. Afterwards every
managed service that quietly depends on it (etcd, ZooKeeper, Kafka's controller, every cloud
database's failover) reads differently.

**Terms** are the key idea, and they are simpler than they look. A term is a logical clock that
only ever increases. Each term has **at most one leader**. A node that sees a term higher than its
own immediately becomes a follower and adopts it - which is how a leader that was partitioned away
discovers it has been replaced, **with no global clock anywhere**. That single rule resolves split
brain.

**Election.** A follower that hears no heartbeat before its timeout increments its term, becomes a
candidate, votes for itself, and asks everyone for a vote. A majority makes it leader.

**One vote per term** is what guarantees at most one leader, and the proof is the same overlap
argument as yesterday's quorum: a win needs a majority, two majorities of the same set must
overlap, and the overlapping node would have had to vote twice. Refusing the second vote is the
whole safety property.

**Randomised timeouts.** If every follower timed out simultaneously they would all become
candidates, split the vote, and nobody would win - repeatedly. Randomising makes one node reliably
go first. That is Day 72's jitter, solving its third distinct problem in this course. It is
remarkable how often "add noise to a shared deadline" is the fix.

**The trade-off:** consensus costs a majority round trip per decision, and it stops entirely
without a majority. That is why you use it for *metadata* - who is the leader, what is the cluster
membership - and not for every write. Systems that need both put consensus underneath and keep the
data path out of it.

## Build (25 min)

Implement `RaftNode` in `src/main/java/sd/p08/day76/`:

- `startElection` - bump the term, become CANDIDATE, vote for self.
- `handleVoteRequest` - refuse a lower term (reporting yours); adopt a higher one and revert to
  follower; then grant only if you have not already voted this term.
- `receiveVote` - ignore older terms, step down on a higher one, count grants, win on a majority.
- `receiveHeartbeat` - a leader at or above your term suppresses your election; a stale one is
  ignored.

This is only the election half of Raft. Log replication is the other half, and it is where most of
the protocol's real complexity lives.

## Reflect (10 min)

1. Explain in two sentences why two nodes cannot both be leader in term 5.
2. The split-vote test left nobody elected in that round. What happens next in a real cluster, and
   why does randomisation make it terminate?
3. A leader is partitioned away for 30 seconds and then returns. Trace exactly what it learns and
   when.

**Interview angle:** "leader election via Raft, so failover is automatic and there is provably one
leader per term" is strong. Being able to explain *why* one-vote-per-term gives you that is
stronger, and takes fifteen seconds.

## Stretch

Add the second half: append-entries with a commit index, so a leader replicates a log and entries
commit once a majority has them. That is the mechanism behind every consensus-backed system you
have ever used.

## Checkpoint

```powershell
.\day.cmd 76
```
