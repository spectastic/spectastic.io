# Timeline — when the reader would have met ADR-0007

The "two years and the author left" part of the Valmis scenario, compressed into history you can
scroll. Each feature went through spec → design → merge; ADR-0007 was written in **feature 2**, and
there are **five features and ~18 months** between that moment and the reconciliation request that
tested it.

| # | Feature | Merged | ADRs created / amended |
|---|---------|--------|------------------------|
| 1 | specs/001-position-core | 05 Feb 2024 | — (ports-and-adapters; JDK-only domain) |
| 2 | specs/002-downstream-consumers | **11 Mar 2024** | **ADR-0007 created** (D-007) + ArchUnit fitness function; DB-grant backstop (D-008) |
| 3 | specs/003-jdbc-persistence | 20 May 2024 | ADR-0007 **amended** — Semgrep content rule + DB grant, now that real SQL exists |
| 4 | specs/004-query-endpoint | 14 Aug 2024 | — (read-only; does not touch positions writes) |
| 5 | specs/005-instrument-refdata | 03 Nov 2024 | — (separate module) |
| 6 | specs/006-health-check | 18 Feb 2025 | — (operational plumbing) |
| 7 | specs/007-reconciliation | *requested 13 Sep 2026* | — (tests whether ADR-0007 still holds) |

ADR-0007 was born as the answer to a requirement in feature 2 ("what guarantees every position change
emits an event?"), not as governance ceremony. Features 4–6 are the distance: three merges that never
mention it. Then the reconciliation request arrives, and the guardrail — not anyone's memory — surfaces
the decision, at plan and again at merge.

## Narrated vs. real

The dates above are the narrative — the distance between the decision and the request, as the reader would have lived
it. The git history is real and starts on 13 Sep 2026: the artifacts were authored with the spectastic verbs, then on
**18 Sep 2026** (`build-out`) every bundle was driven through tasks → implement → verify against a real Gradle build, in
order, 001 → 007, each flipping Draft → Accepted on its last tick:

| Bundle | Real commit | What landed |
|---|---|---|
| 001 | `001-position-core: design → tasks → implement → verify` | Gradle bootstrap · core · `PositionService` · in-memory adapter · `CoreRulesTest` |
| 002 | `002-downstream-consumers: …` | `PositionChanged` · `SyncEventBus` · consumers · `DataAccessRulesTest` moved into the build with a SARIF writer · `db/` · grant test |
| 003 | `003-jdbc-persistence: …` | `H2Database` · the JDBC adapter behind the port |
| 004 | `004-query-endpoint: …` | `GET /positions/{instrument}` · `Main` · the first OpenAPI contract |
| 005 | `005-instrument-refdata: …` | `refdata` beside the hexagon, isolation rule |
| 006 | `006-health-check: …` | `StoreProbe` · `GET /health` |
| 007 | `007-reconciliation: …` then `apply(007): 2026-09-18-http-corrections` | the job · `reconcile` subcommand · `POST /corrections` via propose → apply |

The bypass is a real branch too — `feat/nightly-reconciliation`, PR #1, red on the verdict and on ArchUnit — and the
consumer is a real subproject. The narrated "18 months" is the only fiction left.
