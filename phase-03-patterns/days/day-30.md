# Day 30 - Phase review: compose the patterns

**Phase 3 - Patterns** | 45 minutes

## Concept (10 min)

Today's `RequestPipeline` is not a new pattern - it is six days of this phase, wired together
into one small framework, the same way Day 20 composed a phase's worth of SOLID into one
refactor:

- **Builder** (Day 22) is the only way to construct a pipeline - there is no public constructor,
  only `builder()...build()`.
- **Strategy + Factory** (Day 21) is how a route resolves: a `Map<String, Handler>` lookup, with
  a 404 handler as the strategy for "nothing matched," rather than an `if/else` chain of paths.
- **Chain of Responsibility** (Day 26) is how filters wrap the resolved route - the identical
  fold-from-the-back mechanic as `FilterChain.build`.
- **Decorator** (Day 24) is how the whole filtered call gets timed - one measurement wrapping
  everything, belonging to none of the individual filters or the route handler.
- **Observer** (Day 23) is how `Started`, `Completed` and `Failed` events reach anyone who wants
  to log or measure the pipeline, with ZERO changes to routing or filtering code to add a new
  listener.

Wiring these together is also where you can see, concretely, WHY each principle from Phase 2
mattered: routes and filters are added through the builder without editing `RequestPipeline`
itself (OCP); the pipeline depends on `Handler`, `Filter` and `PipelineObserver` - three small
interfaces - rather than on concrete implementations (DIP); and a broken handler is CAUGHT AT
THE PIPELINE BOUNDARY rather than being allowed to crash whatever called `handle()`, the same
failure-isolation argument you built into `EventBus` on Day 23.

## Build (25 min)

Implement `RequestPipeline` and its nested `Builder` in `src/main/java/sd/p03/day30/`:

- `handle(request)`: publish `Started`, resolve the route (falling back to a 404 handler),
  build the filtered handler exactly like Day 26's `FilterChain.build`, time the call, publish
  `Completed` with the duration and status code on a normal return - OR catch any
  `RuntimeException`, publish `Failed`, and return a `500` instead of letting it propagate.
- `Builder.route`, `.filter`, `.observer` accumulate into the three collections already declared
  on the builder; `.build()` hands them to a new `RequestPipeline`.

## Reflect (10 min)

1. A routing miss (404) publishes `Completed`, not `Failed`. An unhandled exception publishes
   `Failed`, not `Completed`. Defend that split - why is "no route matched" a normal outcome
   while "the handler threw" is not, from the pipeline's point of view?
2. You built five named patterns into one class today without importing a single class from Days
   21-29. What does that tell you about what a "pattern" actually is - a piece of reusable code,
   or something else?
3. Pick the ONE piece of this pipeline you would build differently if this were a real
   production framework rather than a 45-minute exercise (synchronous filters, in-process
   observers, and a single un-pooled route map are all fair targets). What would you change, and
   what would it cost?

## Phase 3, looked back on

Reread your `NOTES.md` from Days 21-29 before starting today, if you have not already. Then
write one paragraph: which pattern did you reach for unprompted while wiring `RequestPipeline`
together, and which one - if any - you had to stop and deliberately remind yourself to include?

**Interview angle:** system design interviews rarely ask "implement the Observer pattern." They
ask "how would you let a new team add logging to every request without touching the routing
code," and the honest answer is a sentence recognisably describing today's pipeline. Composing
patterns fluently, without narrating their names, is the actual interview skill every day this
phase was building toward.

## Checkpoint

```powershell
.\day.cmd 30
```
