# Demonstrations — the eight behaviors, against feature 7

Every command is reproducible from the repo root. The outputs here are real tool output, committed so a
reader can check every claim offline. The guardrail tooling is specs 112–119 of the spectastic meta-repo.

| # | Behavior | Evidence |
|---|----------|----------|
| 1 | Happy path — plan surfaces ADR-0007 via scoped retrieval | `1-happy-retrieval.txt`, `1-retrieval-log.json` |
| 2 | Forced bypass, caught at plan (design omits the acknowledgment) | `2-plan-bypass.txt` |
| 3 | Forced bypass, caught at merge, **explained for the reviewer** (`--explain`) | `3-merge-bypass.txt`, `3-verdict.json` |
| 4 | The residual case — allowed adapter passes; ArchUnit finding via SARIF | `4-residual.txt`, `4a`/`4b` |
| 5 | Coverage report — the DB grant is a `none`-with-reason | `5-coverage.txt` |
| 6 | Vendor neutrality — the verdict is a plain artifact | `6-vendor-neutrality.md` |
| 7 | Teaching on failure — the explanation, then one Socratic question, never a fix | `7-teaching.txt`, `7-verdict.json` |
| 8 | **Ownership, not layering** — a non-owner service's well-layered write to the store is flagged; the owner's is not | `8-cross-service.txt`, `8-verdict.json`, `8-verdict-as-owner.json`, [`8-consumer-service/`](8-consumer-service/) |

Demo 3 uses `--explain` (spec 118): the terse `VIOLATION` line is followed by a reviewer-grade block in
the shape of a spectastic triage card — the **offending code** shown in context with the flagged line
marked, the **governing decision** made readable (title, reason, the full Context/Decision/Consequences
ADR body, the `KB-0001` domain grounding, and the addressable coordinate
`spectastic://briancorbin/position-keeper-guardrails/decision/002-downstream-consumers/D-007`), and the
**sanctioned path** where the pattern is permitted instead. Every word is composed from the decision
record and the changed file — no model is on the path, and the exit code and `3-verdict.json` are
byte-identical with or without the flag (the explanation is output-only). This is what lets a PR author
or reviewer act without leaving the diff to go read the ADR.

Demo 4 shows the detector/explainer split: spectastic's own content detector *and* ArchUnit's
type-graph finding (ingested from SARIF, never run by spectastic) both compose the **same** decision's
reason; demo 4a shows the sanctioned adapter's identical SQL is silent because it is in the allowed path.

Demo 8 is spec 119 (decision resource scope). D-007 now also names the *store* it governs and its *owner*
(`spectastic://briancorbin/position-keeper-guardrails/datastore/positions`, owned by this project). In the
owner repo that changes nothing — demos 1–7 reproduce byte-identically, the path rule applies. In
`8-consumer-service/` (project `acme/reconciliation-service`) the same decision, evaluated under a non-owner
identity, flags a textbook persistence adapter that writes the store — a path glob would call it sanctioned;
the defect is *whose store it is*, not where the write sits. Flip `spectastic.json` to the owner identity and
the identical file is clean. **Honest scope:** the consumer carries a verbatim copy of D-007 because the
federated cross-repo read is deferred — the verdict reads only its own checkout's `specs/*/design.html`; and
the touch detector is still the content pattern (an API call, a topic, a migration is not a SQL string).
