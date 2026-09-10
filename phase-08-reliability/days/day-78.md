# Day 78 - Observability

**Phase 8 - Reliability** | 45 minutes

## Concept (10 min)

Three pillars, and what each is actually for:

- **Metrics** - cheap, aggregated numbers. Constant cost regardless of traffic, so you can have
  them always on. They tell you **that** something is wrong.
- **Logs** - detailed, per-event, expensive at volume. They tell you **what** happened in one case.
- **Traces** - one request's path across services. They tell you **where** the time went.

Metrics first, because they are the only one you can afford for everything.

The instrument types matter, and the wrong choice is a common and expensive mistake:

- **Counter** - monotonically increasing. Requests, errors, bytes. You query the *rate*.
- **Gauge** - goes up and down. Queue depth, connections, memory. Sampled on read.
- **Timer** - duration plus count, and it can publish **percentiles**. Use this for latency,
  always. A "mean latency" gauge is the mistake: as Day 60 showed, the mean describes nobody's
  experience, and **once you have averaged you can never recover the tail**.

**Tags** are the real power - a counter tagged by endpoint and status lets you ask questions nobody
anticipated. They are also the real danger: every distinct tag combination is a separate time
series. Tag by user id and you have created millions of series - **cardinality explosion**, and a
one-line change that takes down your monitoring system. Tag by bounded values only: endpoint,
status, region. Never by ids.

One more rule that sounds obvious and is regularly violated: **record the metric on the failure
path too.** A timer that only fires on success hides exactly the incident you needed it for.

## Build (25 min)

In `src/main/java/sd/p08/day78/InstrumentedService.java`:

1. `handleRequest` - time the call with a `Timer` tagged `endpoint` and `outcome`
   (`success`/`error`), record the duration whichever way it goes, and rethrow on failure.
2. `recordQueueDepth` - register a gauge over the supplied supplier.

The last test renders the Prometheus exposition format, so you can see what a scrape actually
looks like. `infra/docker-compose.yml` already has Prometheus and Grafana configured to scrape
`/metrics` on ports 8081-8083 if you want to wire this into a real dashboard.

## Reflect (10 min)

1. You have a "mean response time" dashboard. What question can it not answer, and what would you
   replace it with?
2. The cardinality test produced 10 series. Work out how many you would get tagging by user id,
   and what that costs.
3. Your service calls five others and one is slow. Which pillar tells you *that*, which tells you
   *which*, and which tells you *why*?

**Interview angle:** "a timer per endpoint with percentile publication, tagged by outcome, plus
gauges for queue depth - and we are careful with tag cardinality" is specific and operational. The
cardinality clause is what marks it as experience rather than reading.

## Stretch

Start the stack - `docker compose -f infra/docker-compose.yml up -d prometheus grafana` - expose
`prometheus.scrape()` on an HTTP endpoint, and build a dashboard showing request rate, error rate
and p99. Those three panels are the "RED method", and they are most of what an on-call engineer
looks at.

## Checkpoint

```powershell
.\day.cmd 78
```
