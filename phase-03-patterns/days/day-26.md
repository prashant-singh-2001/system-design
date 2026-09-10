# Day 26 - Command + Chain of Responsibility: request pipelines

**Phase 3 - Patterns** | 45 minutes

## Concept (10 min)

**Chain of Responsibility**: a request passes through a sequence of handlers, each free to
handle it, pass it along, or both. Today's `Filter` is that idea in its purest form -
`apply(request, next)` - where `next` is "everything after me in the chain," indistinguishable
from the final endpoint itself. That indistinguishability is what makes `FilterChain.build`
possible with almost no code: each filter just needs ONE `Handler` to call, and it does not
matter whether that handler is another filter or the real logic at the end.

**Command**: bundle "a request to be executed" into an object you can pass around, queue, log,
or undo, rather than as an immediate imperative call. `next` in today's `Filter` IS a command -
`AuthenticationFilter` receives "run the rest of the pipeline with this request" as a single
value, and decides for itself whether to invoke it, right now, or not at all. You are not
building a separate `Command` class today because the pattern already lives inside `Handler`
itself - recognising Command hiding inside a design you would not have labelled that way on
sight is the actual skill.

The pipeline this builds - authenticate, then rate-limit, then handle - is the same shape behind
every API gateway, servlet filter chain, and Express/Koa middleware stack you have used. Order
is not incidental: `authenticationRunsFirst` proves a bad key wins over an exhausted rate limit
BECAUSE authentication was registered first, not because 401 outranks 429 in some universal
sense.

## Build (25 min)

In `src/main/java/sd/p03/day26/`:

1. **`AuthenticationFilter`** - 401 immediately on an unrecognised key, otherwise delegate.
2. **`RateLimitFilter`** - a flat per-IP counter; 429 once a client is at the limit, otherwise
   count and delegate.
3. **`FilterChain.build(filters, terminal)`** - fold the filters around the terminal handler,
   BACK TO FRONT, so `filters.get(0)` ends up outermost - the first thing a request meets.

## Reflect (10 min)

1. `FilterChain.build` has to fold back-to-front. Trace through what goes wrong - concretely,
   which filter ends up wrapping which - if you fold front-to-back instead.
2. `newFilterComposesWithoutModifyingExistingOnes` adds a logging filter as a one-line lambda,
   touching neither `AuthenticationFilter` nor `RateLimitFilter`. Which SOLID principle from
   Phase 2 is this, and which day taught it?
3. Today's rate limiter has no time window - it counts forever, for the life of the filter
   instance. What specifically would break in production if you shipped this as-is, and what is
   the one piece Day 33's token bucket adds to fix it?

**Interview angle:** "middleware chain" and "Chain of Responsibility" are the same answer in two
vocabularies - the interviewer is checking whether you can build the composition (the
fold-from-the-back mechanic), not whether you know the GoF name. Being able to say precisely
why registration order determines execution order, with a concrete example like today's
auth-vs-rate-limit test, is the stronger signal.

## Stretch

Add a `LoggingFilter` that records `(path, statusCode, elapsedNanos)` for every request that
passes through it - effectively today's `TimingDecorator` (Day 24) re-expressed as a `Filter`.
Register it FIRST, so it measures the whole chain, and confirm it needs to know nothing about
authentication or rate limiting to do so.

## Checkpoint

```powershell
.\day.cmd 26
```
