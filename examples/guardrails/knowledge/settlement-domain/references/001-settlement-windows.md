---
slug: 001-settlement-windows
origin: Securities operations reference (house convention)
origin-url: https://www.sec.gov/tm/shortening-securities-transaction-settlement-cycle
edition: 2026-09-13
license: CC-BY-4.0
converter: hand-authored
content-hash: sha256-placeholder
status: not-yet-spot-checked
---

# Settlement windows and custodian reconciliation

A trade executes on the **trade date** (T) but the exchange of cash and securities completes on the
**settlement date**. Under the US T+1 regime (effective 28 May 2024) settlement is one business day
after the trade; many other markets remain on T+2.

A position keeper books positions on **trade date** — a bought position exists the instant the trade
is booked. The **custodian**, which moves the actual securities, reflects a position only once it has
**settled**. The two therefore diverge intraday and across the settlement window: pending settlements,
failed trades, and corporate actions all create drift.

The custodian sends an **end-of-day position file** reflecting its settled book. Reconciliation
compares the position keeper's trade-date book against the custodian's settled snapshot and corrects
the difference. Because the correction changes a position, it must flow through the same emission path
every other position change uses — so the audit trail records why the position moved and downstream
consumers (Risk, P&L) stay consistent.
