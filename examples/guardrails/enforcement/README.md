# enforcement/

ADR-0007's fitness functions (`specs/002-downstream-consumers/design.html#D-007`) and their **real** output.

## What lives where

| Thing | Where | Notes |
|---|---|---|
| The ArchUnit boundary rule `only_the_persistence_adapter_touches_data_access_apis` | `src/test/java/xyz/briancorbin/pk/enforcement/DataAccessRulesTest.java` | Moved here from this directory in 002 (T-101) so the Gradle build runs it. A plain `@Test` — see below for why. |
| The ArchUnit → SARIF writer | `src/test/java/xyz/briancorbin/pk/enforcement/SarifReport.java` | ArchUnit ships no SARIF emitter and `@ArchTest` offers no result hook, so the test evaluates the rule itself and writes `build/reports/archunit/archunit-results.sarif` on every run — clean or not. One result per access; `uri` is the source path under `src/main/java`, `startLine` the `(File.java:NN)` ArchUnit cites. |
| The Semgrep content rule `no_sql_write_to_positions_outside_adapter` | `semgrep-positions.yaml` | The residual case the type-graph rule cannot see: a legit-JDBC file writing `positions`. |
| `archunit-results.sarif` | this directory | **Real output** from `./gradlew test --tests '*DataAccessRulesTest'` on `feat/nightly-reconciliation` (PR #1, the Priya bypass). Six results at `src/main/java/xyz/briancorbin/pk/recon/PositionReconciliationJob.java:21–26`, one per `java.sql` access. |
| `semgrep-results.sarif` | this directory | **Real output** from `semgrep --config enforcement/semgrep-positions.yaml --sarif src/` on the same branch. One result at `:22`. |

## How the SARIFs were produced

```sh
git checkout feat/nightly-reconciliation
./gradlew test --tests '*DataAccessRulesTest'          # red; writes build/reports/archunit/archunit-results.sarif
semgrep --config enforcement/semgrep-positions.yaml --sarif -o build/semgrep.sarif src/
```

The branch's copy of the job is `docs/priya-pr/PositionReconciliationJob.java` after `./gradlew spotlessApply` — the
build's formatter expands the one-line constructor, so `UPDATE positions` sits on line **22** there and on line **20** in
the quoted `docs/priya-pr/` copy the demonstrations use. Same bytes otherwise.

## Feeding them to the verdict

```sh
spectastic verdict --changed src/main/java/xyz/briancorbin/pk/recon/PositionReconciliationJob.java \
  --enforcer-output enforcement/archunit-results.sarif
```

joins each result's `ruleId` to D-007's `<spec-rule id=…>` and reports one enforcer violation per result beside the verdict's
own content-detector finding. Two things to know:

- **Semgrep prefixes its rule ids** with the config file's path stem — `enforcement.no_sql_write_to_positions_outside_adapter`
  — so its SARIF does **not** join on the exact id the decision declares and is silently ignored by `--enforcer-output`
  (meta-repo inbox I-091). The verdict still catches the write, through its own `pattern=` detector; the Semgrep SARIF is
  kept as the honest artefact of what the tool emits. Passing `--sarif` output through a `ruleId` rewrite, or declaring the
  prefixed id, are the two workarounds.
- **`--enforcer-output` must be a path relative to the project** — an absolute path is joined under the cwd and reported as
  unreadable (meta-repo inbox I-092).

## In a multi-project build

`./gradlew test --tests '*DataAccessRulesTest'` — the command D-007 records as `run=` — also runs against the
`reconciliation-service` subproject, where no such test exists. Gradle fails a `test` task whose filter matches nothing, so
that subproject sets `test { filter.failOnNoMatchingTests = false }` (stage 10 of the build-out).
