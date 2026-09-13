# Demo 6 — vendor neutrality

The gate output is a **plain JSON artifact from a deterministic Node CLI**, not a message from any one
vendor's model. The same `verdict.json` (`3-verdict.json`, `7-verdict.json`) is what a hook in any tool
feeds back into its loop — Claude Code (Stop / pre-tool), Cursor, Codex, Copilot, or a plain CI script.
The committed, reproducible pieces:

1. `../AGENTS.md` — points at `spectastic adrs --for` and `spectastic verdict`; restates no ADR.
2. `3-verdict.json` — the verdict, a plain artifact you can diff, archive, or feed to any tool's hook.
3. The enforcement CLI is `node …/spectastic verdict`, which runs under any tool or none.

**Honest scope:** the committed verdict artifacts are real tool output. This example does not ship live
transcripts from two separate agent products; the neutrality claim rests on the artifact being plain,
deterministic JSON with no tool-specific coupling — re-run the command and diff the result.
