# Day 3 - Little's Law and the queueing knee

**Phase 1 - Foundations** | 45 minutes

## Concept (10 min)

**Little's Law:** `L = lambda x W`. Concurrency equals throughput times latency.

It holds for any stable system, with no assumptions about how arrivals are distributed. It is
how you answer "how big should this thread pool be?" without guessing: 1,000 requests per second
at 50 ms each means 50 requests in flight, so 50 workers. It also gives you the ceiling in the
other direction - 50 workers at 50 ms can never exceed 1,000 req/s, no matter what you tune.

Then the part that changes how you think. For an M/M/1 queue, mean response time is:

```
W = 1 / (mu - lambda)
```

Look at what happens as arrival rate approaches service rate. At 50% utilisation, response time
is 2x service time. At 90%, it is 10x. At 99%, it is 100x. The curve does not bend gently - it
goes vertical.

That is why you run servers at 60-70% utilisation. The other 30% is not waste, it is the entire
difference between a p99 of 50 ms and a p99 of one second. It is also why autoscaling on CPU at
an 80% threshold routinely fires too late.

**The trade-off:** headroom costs money and buys you latency stability. How much you buy is a
business decision, but it should be a decision, not an accident.

## Build (25 min)

Implement the five methods in `src/main/java/sd/p01/day03/LittlesLaw.java`.

The two queueing methods must throw `IllegalArgumentException` when `arrivalRate >= serviceRate`.
That is not defensive padding - the queue genuinely has no finite mean in that regime, and
returning a number would be a lie. Refusing to answer *is* the correct answer.

## Reflect (10 min)

1. Your service handles 200 req/s with a p50 of 100 ms. How many requests are in flight? What
   pool size would you configure, and what headroom would you add?
2. A dependency slows from 20 ms to 60 ms and your pool is fixed. What happens to your maximum
   throughput? Trace it through Little's Law.
3. Run `printUtilisationCurve(100)`. Where would you set an autoscaling threshold, and why not
   higher?

**Interview angle:** "we will run at about 70% utilisation" is a good answer. "We will run at
70% because the M/M/1 response curve goes vertical past that, and I would rather pay for
headroom than for a p99 cliff" is a much better one.

## Stretch

Little's Law also applies to queues of *work*, not just requests. If your Kafka topic receives
10,000 messages/s and each takes 5 ms to process, how many consumer threads keep the lag at
zero? What happens to lag at 9,999 messages/s versus 10,001?

## Approach, if you are stuck

- `concurrencyNeeded` = throughput x latency
- `maxThroughput` = concurrency / latency
- `utilisation` = arrival / service
- `averageResponseTime` = `1 / (serviceRate - arrivalRate)` after the stability check
- `averageQueueLength` = `rho x rho / (1 - rho)` after the stability check

## Checkpoint

```powershell
.\day.cmd 3
```
