---
name: finance-settlement
description: Securities-settlement and cash-accounting domain knowledge for grounding cash-snapshot specs — settlement cycles (T+1/T+2), books of record (IBOR/ABOR/PBOR), FX settlement risk, and cash-account snapshot semantics.
---

# finance-settlement

Domain knowledge for securities settlement and cash accounting, curated so a
spectastic plan can ground a design-bearing **domain** fact — a settlement
window, a book's cash basis — against a document a reviewer can open, pinned to
the exact edition it was read against.

## Shape

- `SKILL.md` (this file) — the discovery layer an agent reads first.
- `index.md` — a curated map from `KB-NNN` id to title, description, current
  edition, and path. Cheap to read before pulling any document.
- `references/` — the documents themselves, each carrying provenance
  frontmatter (`origin`, `origin-url`, `edition`, `license`, `converter`,
  `content-hash`, `status`) and a stable `KB-NNN` id independent of file path.
- `references/superseded/` — retained prior editions, never overwritten, so an
  edition-pinned citation to an older edition (e.g. `KB-001@2017-09-05`) always
  resolves.

## Citing

A plan decision cites a document as `KB-NNN@edition` — the id pinned to the
exact edition it was grounded against, so a later re-ingest at a newer edition
can never silently change what a historical decision claimed to have read. The
settlement cycle is the worked example: `KB-001@2017-09-05` (T+2) is the edition
in force before 28 May 2024; `KB-001@2024-05-28` (T+1) supersedes it.
