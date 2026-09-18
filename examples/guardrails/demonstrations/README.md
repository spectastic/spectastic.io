# Demonstrations — the eight behaviors, against feature 7

Every command is reproducible from the repo root (demo 8 from `services/reconciliation-service/`). The outputs here are real tool output, committed so a
reader can check every claim offline. The guardrail tooling is specs 112–120 of the spectastic meta-repo.

| # | Behavior | Evidence |
|---|----------|----------|
| 1 | Happy path — plan surfaces ADR-0007 via scoped retrieval | `1-happy-retrieval.txt`, `1-retrieval-log.json` |
| 2 | Forced bypass, caught at plan (design omits the acknowledgment) | `2-plan-bypass.txt` |
| 3 | Forced bypass, caught at merge, **explained for the reviewer** (`--explain`) | `3-merge-bypass.txt`, `3-verdict.json` |
| 4 | The residual case — allowed adapter passes; **real** ArchUnit findings via SARIF from PR #1 | `4-residual.txt`, `4a`/`4b` |
| 5 | Coverage report — the DB grant is a `none`-with-reason | `5-coverage.txt` |
| 6 | Vendor neutrality — the verdict is a plain artifact | `6-vendor-neutrality.md` |
| 7 | Teaching on failure — the explanation, then one Socratic question, never a fix | `7-teaching.txt`, `7-verdict.json` |
| 8 | **Ownership, not layering** — a non-owner service's well-layered write is flagged with `cause: "ownership"`, explained and taught as an ownership question; the owner's is not | `8-cross-service.txt`, `8-verdict.json`, `8-verdict-as-owner.json`, [`services/reconciliation-service/`](../services/reconciliation-service/) |

Demo 3 uses `--explain` (spec 118): the terse `VIOLATION` line is followed by a reviewer-grade block in
the shape of a spectastic triage card — the **offending code** shown in context with the flagged line
marked, the **governing decision** made readable (title, reason, the full Context/Decision/Consequences
ADR body, the `KB-0001` domain grounding, and the addressable coordinate
`spectastic://briancorbin/position-keeper-guardrails/decision/002-downstream-consumers/D-007`), and the
**sanctioned path** where the pattern is permitted instead. Every word is composed from the decision
record and the changed file — no model is on the path, and the exit code and `3-verdict.json` are
byte-identical with or without the flag (the explanation is output-only). This is what lets a PR author
or reviewer act without leaving the diff to go read the ADR.

Demo 2 is **forward-only** (114): the plan-constraint fires on a *Draft* design and never on an Accepted one. It was
captured while 007's design was still Draft — by removing the "Governing decisions acknowledged" paragraph, running
`validate`, and restoring it — and cannot be reproduced against `main`, where every bundle is Accepted. To reproduce it in a
scratch checkout: set all three of 007's status pills (spec, design, tasks) to `draft` — a split bundle is a `status-disagreement`
error that pre-empts the scan — and strip every mention of `002-downstream-consumers/D-007` from the design (the
acknowledgment paragraph, the risk row, D-001's context and the changelog), because any textual mention of the coordinate
counts as an acknowledgment; then run `spectastic validate 'specs/**/*.html' '*.html'`. The `→` fix-hint
line joined the capture on 18 Sep 2026; the CLI prints it now.

Demo 4 shows the detector/explainer split: spectastic's own content detector *and* ArchUnit's
type-graph findings (ingested from SARIF, never run by spectastic) both compose the **same** decision's
reason; demo 4a shows the sanctioned adapter's identical SQL is silent because it is in the allowed path.
Since the build-out the SARIF is **real output** of `./gradlew test --tests '*DataAccessRulesTest'` on the bypass
branch `feat/nightly-reconciliation` (PR #1): six results, one per `java.sql` access, at
`src/main/java/xyz/briancorbin/pk/recon/PositionReconciliationJob.java:21–26` — the branch's copy is
Spotless-formatted, so its `UPDATE` sits on line 22 where the quoted `docs/priya-pr/` copy has it on 20. The
native detector's `:20` and the enforcer's `:21–26` therefore name the same statement in two copies of one file;
see [`enforcement/README.md`](../enforcement/README.md).

Demo 8 is spec 119 (decision resource scope). D-007 now also names the *store* it governs and its *owner*
(`spectastic://briancorbin/position-keeper-guardrails/datastore/positions`, owned by this project). In the
owner repo that changes nothing — demos 1–7 reproduce byte-identically, the path rule applies. In
[`services/reconciliation-service/`](../services/reconciliation-service/) (project `acme/reconciliation-service`, a real
Gradle subproject since the build-out; `8-consumer-service.md` is the pointer) the same decision, evaluated under a non-owner
identity, flags a textbook persistence adapter that writes the store — a path glob would call it sanctioned;
the defect is *whose store it is*, not where the write sits. Flip `spectastic.json` to the owner identity and
the identical file is clean. Spec 120 makes the *output* ownership-aware too: the violation carries
`cause: "ownership"` plus the store's `owner` and `storeCoordinate` (set once, in the verdict — the only
place the owner comparison happens); `--explain` replaces "sanctioned path" with "owned elsewhere — route the
change through its owner", and `--teach` asks why the data needs to live here rather than being routed through
the owning service. Demos 1–7 carry `cause: "path"` and render exactly as before. **Honest scope:** the consumer carries a verbatim copy of D-007 because the
federated cross-repo read is deferred — the verdict reads only its own checkout's `specs/*/design.html`; and
the touch detector is still the content pattern (an API call, a topic, a migration is not a SQL string).
