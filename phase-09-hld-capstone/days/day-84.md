# Day 84 - Design: news feed

**Phase 9 - HLD and capstone** | 45 minutes

## Concept (10 min)

You computed the pivotal number on Day 10. Today it becomes a design.

**Fanout-on-write** copies each post into every follower's precomputed inbox. Reads become a single
lookup, which matters enormously in a 50:1 read-heavy system. But the write load is
`posts/second x average followers` - 4,000 posts/s at 200 followers is 800,000 feed writes per
second. And a 50-million-follower account turns one post into 50 million writes.

**Fanout-on-read** leaves posts in place and merges at read time. Writes are trivial; reads must
gather from everyone you follow and merge. Following 500 accounts means touching 500 sources for
one page.

Every real system uses a **hybrid**, and today's kernel shows why with a number: in a realistic
skew, two accounts out of ten thousand produce almost all the write load. Excluding them collapses
it by orders of magnitude.

The cost is honest and permanent: **two code paths, forever**, plus a threshold that has to be
tuned and a merge step on every read for the celebrity portion. Nobody chooses this because it is
elegant. They choose it because the follower-count distribution is so skewed that no single
mechanism serves both ends.

The second design question, and the one that separates a thoughtful answer: **what does the feed
contain?** Chronological is trivial to build and nobody ships it any more. Ranked means the feed
depends on a model, which means it cannot be fully precomputed, which pushes you back toward
fanout-on-read whether you wanted it or not. The ranking decision quietly rewrites your
architecture.

## Build (25 min)

**First (about 10 min)** implement `FeedPlanner` in `src/main/java/sd/p09/day84/`: fanout load, the
hybrid rule, the hybrid's actual write load, and the k-way merge that is the read path.

**Then (about 15 min)** write `designs/news-feed.md`. Assume 200M DAU, 2 posts and 100 feed views
per user per day, and a follower distribution with a long tail.

## Reflect (10 min)

1. Defend your celebrity threshold with a number, not a feeling. What does it cost to set it too
   low? Too high?
2. A user follows 5,000 accounts, 40 of them celebrities. Trace exactly what their feed read does.
3. Someone deletes a post that has been fanned out to 10 million inboxes. What happens?

**Interview angle:** get to the fanout arithmetic early. "4,000 posts/s times 200 average followers
is 800,000 writes/s - significant but tractable. The problem is the tail: one celebrity post is 50
million writes. So I would go hybrid." That is a complete, defensible position in twenty seconds.

## Stretch

Design the feed cache: how much of each user's feed do you keep hot, and for whom? Most users never
scroll past the first page, and inactive users should not be fanned out to at all. Both
observations save more than any clever data structure.

## Checkpoint

```powershell
.\day.cmd 84
```
