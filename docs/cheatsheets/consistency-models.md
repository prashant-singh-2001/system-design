# Consistency models

Ordered from strongest to weakest. Stronger costs latency and availability. Pick the weakest
model that satisfies the requirement, and be able to say why it is sufficient.

| Model | Guarantee | Cost | Typical use |
|---|---|---|---|
| **Linearizable** | Every read sees the most recent write; the system behaves like one copy | Consensus round trip on every write | Distributed locks, leader election, balances |
| **Sequential** | All nodes see operations in the same order, not necessarily real time | Cheaper than linearizable | Replicated state machines |
| **Causal** | Causally related operations are seen in order; concurrent ones may differ | Vector clocks or version vectors | Comments and replies, collaborative editing |
| **Read-your-writes** | A client always sees its own writes | Sticky routing or write tracking | Profile edits |
| **Monotonic reads** | A client never sees time go backwards | Session pinning | Feeds, timelines |
| **Eventual** | Replicas converge if writes stop | Almost none | Analytics, counters, caches |

## CAP, stated carefully

During a **network partition** you must choose availability or consistency. That is all CAP
says. It is not a general licence to describe a database as "AP" or "CP" - it is about
partition behaviour specifically.

**PACELC** is the more useful framing:
> If **P**artition, choose **A** or **C**; **E**lse, choose **L**atency or **C**onsistency.

The "else" half is where you live 99.9% of the time, and it is the half CAP ignores.

## Quorum arithmetic

With N replicas, W write acks, R read responses:

- `W + R > N` gives strong consistency for reads
- `W = N, R = 1` - fast reads, slow and fragile writes
- `W = 1, R = N` - fast writes, slow reads
- `W = R = (N+1)/2` - the balanced default; with N=3, W=R=2

Quorum still does not give linearizability on its own - you need read repair or a consensus
protocol on top. Say that if asked; it is a common follow-up.
