# HLD: Social feed

Budget: 45 minutes. The failure mode is spending 25 minutes on requirements. Watch the clock.

## 1. Requirements and scope (5 min)

**Functional:**
- Users can post short text updates.
- Users can follow/unfollow other accounts.
- Users can view a feed composed of posts from accounts they follow, roughly newest-first.

**Non-functional:**
- **Availability target:** 99.9%+ for feed reads — the feed should almost always load, even if
  slightly stale, since reads dominate at 50:1 (Day 2).
- **Latency budget:** p50 < 100 ms, p99 < 300 ms for feed reads (the hot path); writes (posting)
  can tolerate more, since fanout can happen asynchronously.
- **Consistency:** eventual is fine. A follower seeing a new post a few seconds late is
  acceptable; strong consistency is not required and would only cost latency for no real benefit.
- **Durability:** once a post is acknowledged, it must not be lost, even if its fanout to every
  follower's feed hasn't finished yet.

**Out of scope:** ranking/ML-driven ordering, media/video storage, likes/comments, notifications,
search, direct messaging.

## 2. Estimation (5 min)

Carrying forward Day 2's Twitter-scale numbers (200M DAU, 2 writes + 100 reads/user/day):

| Quantity | Value | Working |
|---|---|---|
| DAU | 200,000,000 | given (Day 2) |
| Read QPS (avg / peak) | 200,000 / ~400,000–600,000 | `200M × 100 / 100,000s`; peak is typically 2-3x avg |
| Write QPS (avg / peak) | 4,000 / ~8,000–12,000 | `200M × 2 / 100,000s`; peak is typically 2-3x avg |
| Storage per year | ~44 TB/year (unreplicated) | `120 GB/day × 365`; 5-yr total ≈ 219 TB, ×3 replication ≈ 657 TB (Day 2) |
| Bandwidth | not computed | no average post payload size given in this exercise |

**Feed-specific numbers — the ones that actually decide the architecture:**

| Quantity | Value | Working |
|---|---|---|
| Fanout-on-write load | **800,000 writes/s** | `4,000 posts/s × 200 avg followers` — from only 4,000 posts/s |
| Hot feed cache dataset | 168 GB | `2.8B cached feed items × 300 bytes × 20% hot fraction` (80/20 rule) |
| Cache servers needed | 3 | `ceil(168 GB / 64 GB per server)` — rounds **up** |
| App servers needed | 100 | `ceil(500,000 peak QPS / 5,000 QPS per server)` |
| Hybrid threshold | > 100,000 followers | above this, switch that account to fanout-on-read |

**The number that matters:** 800,000 fanout writes/s from ordinary accounts is significant but
tractable. The real problem is the tail — one post from a 50-million-follower account turns into
50 million writes. That's why the design is a **hybrid**: fanout-on-write below the threshold,
fanout-on-read above it.