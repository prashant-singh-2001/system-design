# HLD: <system>

Budget: 45 minutes. The failure mode is spending 25 minutes on requirements. Watch the clock.

## 1. Requirements and scope (5 min)

**Functional:**
**Non-functional:** availability target, latency budget (p50/p99), consistency needs, durability
**Out of scope:**

## 2. Estimation (5 min)

| Quantity | Value | Working |
|---|---|---|
| DAU | | |
| Read QPS (avg / peak) | | peak is typically 2-3x avg |
| Write QPS (avg / peak) | | |
| Storage per year | | |
| Bandwidth | | |

State the read:write ratio explicitly - it drives nearly every later decision.

## 3. API design (5 min)

```
POST /resource     -> 201
GET  /resource/{id} -> 200 | 404
```

## 4. Data model (5 min)

Tables or collections, keys, and the **access patterns** each one serves.
Choose the partition key here and justify it.

## 5. High-level architecture (10 min)

Client -> LB -> service -> cache -> database, plus queues and workers.
Draw it. Then walk one read and one write through it end to end.

## 6. Deep dive (10 min)

Pick the one or two genuinely hard parts and go deep. This is what you are actually assessed on.
Candidates: hot keys, fanout, ordering, consistency, the bottleneck component.

## 7. Bottlenecks, failure modes, scaling (5 min)

- What breaks first as traffic grows 10x?
- What happens when each component dies?
- Where is the single point of failure?
- How does it degrade gracefully rather than collapse?
