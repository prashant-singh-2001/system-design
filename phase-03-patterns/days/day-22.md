# Day 22 - Builder + Prototype: complex config without telescoping constructors

**Phase 3 - Patterns** | 45 minutes

## Concept (10 min)

`LegacyServiceConfig` has the classic telescoping-constructor smell: several overloaded
constructors, each forwarding to the widest one with made-up defaults filling the gaps. Read
`new LegacyServiceConfig("payments.internal", 8080, 3000, 10)` and try to say which argument is
the timeout without checking the file. You cannot - two `int`/`long` parameters in a row give
you nothing to go on, and swapping them compiles, runs, and silently misconfigures the service.

A **Builder** fixes this by naming every value at the call site (`.port(8080).timeoutMs(3000)`)
and splitting validation into two kinds that genuinely are different questions: a bad `port` is
wrong the instant you see it, so the setter rejects it immediately; a missing `host` is only
knowable once the WHOLE object is assembled, so only `build()` can catch it. Conflating these -
validating everything at the end, or nothing until a NullPointerException three services away -
is where most builder implementations go soft.

A **Prototype** solves a different, related problem: cloning an existing, already-valid object
with one or two fields changed, instead of respecififying everything from scratch. Today's
`toBuilder()` seeds a fresh `Builder` from an existing `ServiceConfig`'s current values -
`prod.toBuilder().retries(5).build()` reads exactly like the sentence you would say out loud.

## Build (25 min)

In `src/main/java/sd/p03/day22/`, implement `ServiceConfig` and its nested `Builder`:

- Per-field validation in the setters: `port` (1-65535), `timeoutMs` (positive),
  `maxConnections` (at least 1), `retries` (non-negative) - each throws
  `IllegalArgumentException` immediately.
- Completeness validation in `build()`: a missing `host` throws
  `IllegalStateException("host is required")`.
- `tls(certPath)` turns TLS on; absent by default.
- `tag(key, value)` accumulates into a map, defensively copied and made unmodifiable at
  `build()` - Day 16's two-doors argument, again.
- `toBuilder()` - the PROTOTYPE: seed a new `Builder` from this instance's current values.

## Reflect (10 min)

1. State precisely why `host` can only be checked in `build()` while `port` is checked in its
   own setter. What is the general rule for sorting a validation into one bucket or the other?
2. `builderIsReusableAfterBuild` builds twice from the same builder and expects two independent
   configs. What would go wrong for callers if `build()` handed out a live reference to the
   builder's own internal tag map instead of a defensive copy?
3. Prototype and Builder solve genuinely different problems, but `toBuilder()` uses a `Builder`
   to do it. Explain in one sentence why Prototype did not need its own separate mechanism here.

**Interview angle:** "use a builder for objects with many optional fields" is table stakes. The
signal is explaining the validation SPLIT unprompted - which checks belong at the setter and
which only make sense at `build()` - because that distinction is exactly what separates a
builder that fails fast from one that produces a half-valid object nobody notices until later.

## Stretch

Add a `merge(ServiceConfig other)` method to `Builder` that overlays every NON-DEFAULT field
from `other` onto the builder's current values - a step toward the kind of layered
config-from-multiple-sources pattern real services actually use (defaults, then a file, then
environment variables, each overriding the last).

## Checkpoint

```powershell
.\day.cmd 22
```
