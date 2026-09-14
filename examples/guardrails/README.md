# position-keeper-guardrails

A worked example proving spectastic's **architectural guardrails** (meta-repo specs 112–119) on a
realistic securities position keeper. It was **scaffolded by `spectastic init` and driven through the
spectastic verbs** — this is a real downstream project, not a hand-placed set of artifacts.

It answers the Valmis scenario directly: an agent-authored PR writes to the positions table, bypassing
the service that emits the audit events. Here the guardrail — not anyone's memory of a two-year-old ADR
— catches it, at plan and again at merge — and, since the decision names the store's owner, in another
service too, where the write is cleanly layered and the defect is ownership.

## How it was built

1. `spectastic init --profile standard` — the real scaffold (`.claude/commands`, `.agents/skills`,
   `AGENTS.md`, `principles.html`, templates, assets, a knowledge-corpus starter).
2. `spectastic corpus import settlement-domain` — a real domain pack; the ingester assigned `KB-0001`
   in `knowledge/index.md`. ADR-0007 and the reconciliation decision ground a domain fact against it
   (`KB-0001@2026-09-13`).
3. The seven features authored through the verbs (spec → design). ADR-0007 lives in
   `specs/002-downstream-consumers/design.html` as `<spec-decision id="D-007">` with scope, enforcement
   rule-ids, posture, review-by, and a reason; the DB-grant backstop (`D-008`) is a `none`-with-reason.
   Amended 13 Sep 2026 with a *resource scope* (spec 119): the decision names the positions store by
   coordinate and this project as its owner, so a non-owner service's touch is flagged however well layered.
4. `spectastic init --tools` installed the guarantee layer (pre-commit `validate` gate).
5. The eight guardrail behaviors run in [`demonstrations/`](demonstrations/README.md) with committed output.

## What's here

- `specs/001…007/` — the seven features; see [`docs/timeline.md`](docs/timeline.md).
- `knowledge/settlement-domain/` — the domain corpus (`KB-0001`), imported not hand-placed.
- `src/…/persistence/` — the sanctioned single writer.
- `docs/priya-pr/PositionReconciliationJob.java` — the bypass (direct `UPDATE positions`).
- `enforcement/` — the ArchUnit fitness function, the Semgrep rule, a SARIF fixture.
- `demonstrations/` — the [eight behaviors](demonstrations/README.md), each reproducible; `8-consumer-service/` is a
  second, non-owner project (`acme/reconciliation-service`) for the cross-service case.

## Honest scope

The **guardrail behavior is real** — every command runs the actual tooling and the outputs are committed.
The Java is **representative** of the target architecture (not a full Gradle build), the git history is
**narrated** in `docs/timeline.md`, and the "CI logs" are the committed tool outputs. The point is not
that a Java project compiles — it is that a decision recorded as data, months before a request that
tests it, is surfaced and enforced deterministically, with no model on the verdict path.
