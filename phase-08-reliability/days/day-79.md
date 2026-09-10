# Day 79 - SLOs, error budgets and chaos

**Phase 8 - Reliability** | 45 minutes

## Concept (10 min)

Start with the numbers, because they reframe every conversation about reliability. Over 30 days:

| Target | Permitted downtime |
|---|---|
| 99% | ~7 hours 12 minutes |
| 99.9% | ~43 minutes |
| 99.99% | ~4 minutes 19 seconds |
| 99.999% | ~26 seconds |

Each extra nine costs roughly ten times the engineering and permits a tenth of the downtime. "Five
nines" is 26 seconds a month - less than a single deploy, or one bad DNS change. Putting that
number next to the request is what makes the conversation honest.

Then the idea that turns reliability from an argument into a number. An SLO of 99.9% does not mean
"try not to fail". It means **you are permitted 0.1% failures, and that permission is a budget you
may deliberately spend**:

- **Budget remaining** -> ship. Take risks, deploy on Friday, run the migration. An unspent budget
  means the target was set too low and you are over-investing in reliability instead of features.
- **Budget exhausted** -> stop shipping features and fix reliability. Not because somebody lost an
  argument, but because an agreed number says so.

Product and engineering stop negotiating from opinion. It is a management technique wearing an
equation.

**Burn rate is what you alert on**, not raw error count. A 1% error rate is exactly on budget at a
99% SLO and a hundred-times-too-fast emergency at 99.99%. The error count means nothing without
the target beside it.

**Chaos engineering** then tests all of this. The argument is simple: your system already fails in
production, at 3am, unobserved. An experiment moves that failure to a Tuesday afternoon when the
people who understand it are awake and the blast radius is chosen rather than discovered.

The discipline that separates it from vandalism: state a hypothesis first, bound the blast radius,
have an abort, and measure against the SLO. An experiment that breaches the SLO is a **successful**
experiment - you learned the limit safely.

## Build (25 min)

In `src/main/java/sd/p08/day79/`:

1. `Slo.allowedDowntime` - window x (1 - target).
2. `ErrorBudget` - allowed failures, consumed and remaining fraction, **burn rate**, and time to
   exhaustion.
3. `ChaosExperiment.run` - inject failures at a rate, count outcomes, and `holdsHypothesis`
   against the SLO.

## Reflect (10 min)

1. Pick a service you work on. What is its SLO? If the answer is "we do not have one", what would
   you propose and how would you justify the number?
2. Your budget is 60% consumed on day 10 of 30. What do you do, and what would you have done at 5%?
3. Design a chaos experiment for something you own: hypothesis, blast radius, abort condition,
   measurement.

**Interview angle:** "we set an SLO, alert on error-budget burn rate rather than raw errors, and
use the remaining budget to decide whether to ship or to stabilise" describes an operating model,
not just a monitoring setup. That distinction lands.

## Stretch

Implement **multi-window burn-rate alerting**: page on a fast burn over a short window (2% of
budget in 1 hour) and ticket on a slow burn over a long one (10% in 3 days). That combination is
Google's published recommendation, and it is how you get alerts that are both timely and not noisy.

## Checkpoint

```powershell
.\day.cmd 79
```
