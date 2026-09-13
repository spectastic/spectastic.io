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
