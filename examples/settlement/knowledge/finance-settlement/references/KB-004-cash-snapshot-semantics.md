---
id: KB-004
origin: Investment-operations market practice — cash-account snapshot semantics
origin-url: TODO
edition: 2024-01-15
license: CC0-1.0
converter: hand-authored
content-hash: sha256:48840aee96dc8f25f387dbcf1ee1f0b67dbe96c0ed4a64071eb11b3990fe524b
status: authoritative
---

# Cash-account snapshot semantics

A **snapshot** is a cash-account balance "as of" a point in time. The same
account has several correct balances at one instant, distinguished by which
trades count:

- **Traded cash** — the effect of every trade executed on or before the as-of
  time (the IBOR view).
- **Settled cash** — the effect of only those trades whose **settlement date** is
  on or before the as-of time (the ABOR view).
- **Available cash** — settled cash net of holds (pending withdrawals,
  collateral, and similar encumbrances).

A correct as-of snapshot applies the **settlement rule in force on each trade's
trade date**, not the rule in force today — otherwise a historical snapshot
would re-settle old trades under a cycle that did not govern them. Cite as
`KB-004@2024-01-15`.
