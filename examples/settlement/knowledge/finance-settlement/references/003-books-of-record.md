---
slug: 003-books-of-record
origin: Investment-operations market practice — investment, accounting, and performance books of record
origin-url: TODO
edition: 2024-01-15
license: CC0-1.0
converter: hand-authored
content-hash: sha256:8df733160c9dc75b903929b21ede27c5faeca5d9540361d382c1fc7f76a0d210
status: authoritative
---

# Books of record — IBOR, ABOR, PBOR

An asset manager keeps several **books of record**, each a view of the same
positions and cash on a different basis:

- **IBOR** (Investment Book of Record) — the real-time / intraday **front-office**
  view. Trade-date basis: it includes unsettled trades the moment they are
  executed, so the desk sees traded cash immediately.
- **ABOR** (Accounting Book of Record) — the official, reconciled book used for
  **NAV and reporting**. Settlement-date basis: it recognises cash only once it
  has actually settled, and is the **golden source for settled cash**.
- **PBOR** (Performance Book of Record) — the performance / attribution book,
  reconciled to ABOR with performance-specific adjustments.

The books differ chiefly in **cash basis and timing**: IBOR shows traded cash
immediately; ABOR shows it once settled. Cite as `KB-003@2024-01-15`.
