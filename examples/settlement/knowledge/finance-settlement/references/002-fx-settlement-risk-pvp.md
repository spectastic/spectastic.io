---
slug: 002-fx-settlement-risk-pvp
origin: Bank for International Settlements, Committee on Payments and Market Infrastructures — FX settlement risk and payment-versus-payment (PvP)
origin-url: https://www.bis.org/cpmi/
edition: 2022-07-08
license: CC0-1.0
converter: hand-authored
content-hash: sha256:c1c05d2a9f9eadf52159c67a0ca231bc9e43be7e2a8c990ba22d3e62a74acde1
status: authoritative
---

# FX settlement risk and payment-versus-payment

An FX trade settles **two cash legs** — one currency paid, the other received.
**Settlement risk** (also "Herstatt risk", after the 1974 failure of Bankhaus
Herstatt) is the risk that one leg pays out while the counter-leg fails to
arrive, exposing the full principal of the trade, not just a mark-to-market
difference.

**Payment-versus-payment (PvP)** settles both legs **conditionally** — neither
leg is final unless both are — which eliminates principal risk. CLS is the
canonical PvP mechanism for FX.

For reporting, exposure is measured on the **principal** of the unsettled leg,
carried with a **PvP flag** recording whether the trade settles through a PvP
arrangement. Cite this document by its registry `KB-NNNN` id, pinned to the `@2022-07-08` edition.
