# position-keeper-guardrails

A worked example proving spectastic's **architectural guardrails** (meta-repo specs 112–121) on a
realistic securities position keeper. It was **scaffolded by `spectastic init` and driven through the full
lifecycle** — spec → design → tasks → implement → verify for every feature, plus one propose → apply — on a
**real two-service Gradle build** with CI. This is a real downstream project, not a hand-placed set of artifacts.

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
3. The seven features authored through the verbs — spec for all seven, design for 002 and 007 on 13 Sep 2026; the other five designs came with the build-out in step 6. ADR-0007 lives in
   `specs/002-downstream-consumers/design.html` as `<spec-decision id="D-007">` with scope, enforcement
   rule-ids, posture, review-by, and a reason; the DB-grant backstop (`D-008`) is a `none`-with-reason.
   Amended 13 Sep 2026 with a *resource scope* (spec 119): the decision names the positions store by
   coordinate and this project as its owner, so a non-owner service's touch is flagged however well layered.
4. `spectastic init --tools --ci github` installed the guarantee layer: the pre-commit `validate` gate, the command
   adapters, and the **managed CI gate** `.github/workflows/spectastic.yml` (validate → enforce → merge verdict on every
   pull request; spec 121) — pinned to the installing CLI and drift-checked by `validate`.
5. The eight guardrail behaviors run in [`demonstrations/`](demonstrations/README.md) with committed output.
6. **Every spec bundle driven to Accepted** (`build-out`, 18 Sep 2026): `/spectastic.design` → `/spectastic.tasks` →
   `/spectastic.implement --all` → the bundled Draft → Accepted flip on the last tick (001 and 002 each confirmed at the
   prompt; 003–007 pre-confirmed in one gesture, as their changelogs say) → `spectastic verify` from the real
   run. 001's setup phase is the Gradle bootstrap (Groovy DSL + Java 21 toolchain, JUnit 5, ArchUnit, Spotless, Checkstyle),
   so the stack is a set of recorded decisions in `specs/001-position-core/design.html`. Seven `tasks.html`, seven
   `verify.html`, 38 tests.
7. **One change proposal**, `/spectastic.propose` → adversarial risk pass → `/spectastic.apply`: 007 gains
   `POST /corrections` (FR-005), the sanctioned door demo 8's verdict tells the consumer to use; its OpenAPI contract was
   promoted from the sidecar to `api/` by the apply verb (spec 071) — the only contract the verb promoted; 004's and
   006's were moved by hand at their flips, because a spec's *first* contract has no promotion path outside `apply`
   (meta-repo I-088). See
   [`specs/007-reconciliation/changes/archive/`](specs/007-reconciliation/changes/archive/).
8. **The bypass is a real branch.** `feat/nightly-reconciliation` compiles the Priya job into the build and is
   [PR #1](https://github.com/spectastic/example-position-keeper-guardrails/pull/1) — left open and red: the managed gate
   fails on the verdict step, the Gradle job on ADR-0007's ArchUnit rule. Its real SARIF is committed under
   [`enforcement/`](enforcement/README.md) and replaces the old hand-written fixture.
9. **The consumer is a real subproject.** [`services/reconciliation-service/`](services/reconciliation-service/) builds
   with the root, routes corrections through the owner's door, and still carries the adapter demo 8 flags for ownership.

## What's here

- `specs/001…007/` — seven bundles (`spec` · `design` · `tasks` · `verify`), all Accepted; 007 also carries an applied
  change; see [`docs/timeline.md`](docs/timeline.md).
- `knowledge/settlement-domain/` — the domain corpus (`KB-0001`), imported not hand-placed.
- `src/main/java/xyz/briancorbin/pk/` — the position keeper: `hex/core` (JDK-only records + ports), `hex/app` (use cases),
  `hex/persistence` (the sanctioned single writer, H2), `hex/events`, `hex/consumers`, `hex/http`, `hex/recon`, `refdata`.
- `api/*.openapi.yaml` — the three promoted contracts (positions, health, corrections).
- `db/` — the store's DDL and D-008's grant, applied at open.
- `docs/priya-pr/PositionReconciliationJob.java` — the bypass (direct `UPDATE positions`), quoted by the demonstrations;
  the same job compiled into the build is PR #1.
- `enforcement/` — the Semgrep rule and the **real** ArchUnit + Semgrep SARIF from PR #1; ADR-0007's ArchUnit rule itself
  lives in `src/test/java/xyz/briancorbin/pk/enforcement/` with its SARIF writer.
- `services/reconciliation-service/` — the second, non-owner project (`acme/reconciliation-service`), a Gradle subproject.
- `demonstrations/` — the [eight behaviors](demonstrations/README.md), each reproducible.
- `inbox.html` — the project's own triage inbox; gaps found in spectastic itself went to the meta-repo's.

Run it: `./gradlew build` · `./gradlew :run` (8080: `/positions/{instrument}`, `/health`, `POST /corrections`) ·
`./gradlew :run --args='reconcile src/test/resources/custodian/eod-2026-09-13.csv'` ·
`./gradlew :reconciliation-service:run` (8081).

## Honest scope

The **guardrail behavior is real** — every command runs the actual tooling and the outputs are committed — and so
is the build: a two-service Gradle project, `./gradlew build` green on `main` (38 tests including the ArchUnit fitness
functions) and red on `feat/nightly-reconciliation` (PR #1), with GitHub Actions running the managed spectastic gate and the
Gradle build on every push and pull request. What is still narrated is the **calendar**: the 2024–25 dates in
`docs/timeline.md` are the story of when each decision would have been met; the real git history is the `build-out`
branch of September 2026. Two smaller honesties: the pre-commit hook `init --tools` installs is local and untracked
(`.git/hooks`), so the tracked gate is CI, not the hook; and the H2 store, the placeholder role passwords and the
deferred authentication are what make this an example rather than a service. The point is not that a Java project
compiles — it is that a decision recorded as data, long before a request that tests it, is surfaced and enforced
deterministically, with no model on the verdict path.
