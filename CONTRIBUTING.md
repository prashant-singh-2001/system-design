# Contributing

This started as a personal 90-day learning curriculum, so most of the daily briefs and
reference solutions reflect one person's path through the material. That said, corrections
and improvements are welcome — a wrong claim in a brief or a flaky test helps nobody.

## What's worth opening an issue or PR for

- **Factual errors** in a day's brief (`phase-NN/days/day-NN.md`) — wrong numbers, wrong
  claims about a language/library/protocol's behavior.
- **Flaky or incorrect tests** — a test that passes when the underlying claim is false, or
  fails intermittently for reasons unrelated to the exercise.
- **Broken tooling** — `day.ps1`/`day.cmd`, the Maven build, or `infra/docker-compose.yml`
  not working as documented.
- **Unclear wording** that would trip up someone doing the exercise cold.

## What's probably out of scope

- Wholesale rewrites of a day's approach or reference solution — the curriculum is
  intentionally opinionated about sequencing and framing.
- Adding new frameworks to Phases 1–8 (see the README's "A note on frameworks" — this is
  deliberate).

## Making a change

1. Open an issue first for anything non-trivial, so we can agree on the fix before you spend
   time on it.
2. For code changes: `mvn -q -pl <phase-module> test` should pass before you open a PR.
3. Keep PRs scoped to one day or one concern — easier to review, easier to revert if wrong.
4. Explain the *why* in the PR description, not just the *what* — especially for anything
   touching a brief's explanation rather than its code.

## Reporting a bug without fixing it yourself

That's fine too — use the issue template and include which day/phase it's in, what you
expected, and what actually happened (test output, error message, etc.).
