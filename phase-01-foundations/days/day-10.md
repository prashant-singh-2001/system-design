# Day 10 - Phase review: estimate a feed

**Phase 1 - Foundations** | 45 minutes

## Concept (10 min)

Start by rereading your `NOTES.md` from days 1-9. Five minutes, properly. This is retrieval
practice and it is the highest-leverage habit in the whole programme - system design knowledge
decays fast without it.

Then today's question, which is the one that decides how every social feed is built:

**Can you fan out on write?**

Fanout-on-write means when someone posts, you immediately copy that post into every follower's
precomputed feed. Reads then become a single lookup - wonderfully fast, and that matters enormously
in a 50:1 read-heavy system.

But the write load is `posts/second x average followers`. With Day 2's numbers - 4,000 posts/s
and 200 average followers - that is 800,000 feed writes per second. From only 4,000 posts.

And then someone with 50 million followers posts, and one write becomes 50 million.

The resolution used by every real system is a **hybrid**: fanout-on-write for ordinary accounts,
fanout-on-read for the handful of accounts above some follower threshold. Celebrity posts stay
in place and get merged in at read time. Two mechanisms, chosen per account, because the
distribution of follower counts is so skewed that one mechanism cannot serve both ends.

**The trade-off:** fanout-on-write buys read latency with write amplification and storage.
Fanout-on-read buys write simplicity with read latency and fan-in complexity. The hybrid buys
both wins with permanent extra complexity - two code paths, forever, and a threshold that has to
be tuned.

## Build (25 min)

Implement `FeedEstimator` in `src/main/java/sd/p01/day10/`. Five short methods:
fanout load, hot-dataset sizing via the 80/20 rule, two capacity calculations that round **up**,
and the one-line hybrid decision rule.

Rounding up is not a detail. 2.1 servers is 3 servers. Interviewers notice which way you round,
because it reveals whether you are treating the arithmetic as a capacity decision or a formality.

## Reflect (10 min)

Then spend the rest of the session writing, not coding. Create
`docs/notes/day-10-feed-estimation.md` using `docs/templates/hld-template.md`, and fill in only
sections 1 and 2 - requirements and estimation. That is what you have the tools for so far. You
will complete the rest of that template on Day 84 when you design the feed properly.

1. At what follower count does fanout-on-write stop making sense? Defend your threshold with an
   actual number rather than a feeling.
2. You computed the hot dataset with the 80/20 rule. What breaks if the real distribution is
   99/1? What if it is 50/50?
3. Look back at your Day 2 answers. Which estimate would you now make differently?

**Interview angle:** the fanout number is the pivot of any feed question. Get to it early:
"before I choose fanout-on-write, let me check the write amplification - 4,000 posts/s times
200 average followers is 800,000 writes/s, which is significant but tractable. The problem is
the tail: a 50-million-follower account makes one post into 50 million writes. So I would go
hybrid." That is a complete, defensible design position in about twenty seconds.

## Phase 1 retrospective

Write in `NOTES.md`:

- The **three numbers** from this phase you will still remember in six months
- The one idea that genuinely surprised you
- The one thing still fuzzy - carry it into Phase 2 explicitly

Then tick days 1-10 in `PROGRESS.md` and commit.

## Checkpoint

```powershell
.\day.cmd 10
```
