---
name: finance-settlement
description: Securities-settlement and cash-accounting domain knowledge for grounding cash-snapshot specs — settlement cycles (T+1/T+2), books of record (IBOR/ABOR/PBOR), FX settlement risk, and cash-account snapshot semantics.
---

# finance-settlement

Domain knowledge for securities settlement and cash accounting, curated so a
spectastic plan can ground a design-bearing **domain** fact — a settlement
window, a book's cash basis — against a document a reviewer can open, pinned to
the exact edition it was read against.

Read the slug map below for the pack's contents, then pull a document from `references/` as the work calls for it. Each reference carries a portable `slug` and its provenance frontmatter; the project-assigned, repo-unique `KB-NNNN` ids live in the root `knowledge/index.md` registry, and citations pin an id to an edition (`KB-NNNN@edition`). Retained prior editions live under `references/superseded/` as `<slug>@<edition>.md`, so an edition-pinned citation to a superseded edition still resolves.

| Slug | Title | Description | Edition | Path |
| --- | --- | --- | --- | --- |
| 001-us-equities-settlement-cycle | US equities settlement cycle | Standard settlement cycle for US equities (T+1), pinned by edition; prior T+2 retained. | 2024-05-28 | references/001-us-equities-settlement-cycle.md |
| 002-fx-settlement-risk-pvp | FX settlement risk and PvP | Herstatt risk and payment-versus-payment. | 2022-07-08 | references/002-fx-settlement-risk-pvp.md |
| 003-books-of-record | Books of record — IBOR, ABOR, PBOR | Investment, accounting, and performance books and the cash basis each uses. | 2024-01-15 | references/003-books-of-record.md |
| 004-cash-snapshot-semantics | Cash-account snapshot semantics | Settled vs available vs traded cash, point-in-time snapshots. | 2024-01-15 | references/004-cash-snapshot-semantics.md |
| 005-corporate-actions-cash | Corporate-action cash entitlements | Ex-date, record date, pay date. | 2023-06-01 | references/005-corporate-actions-cash.md |
