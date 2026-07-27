---
slug: 001-target-size
origin: W3C — Web Content Accessibility Guidelines (WCAG) 2.2, Success Criterion 2.5.8 Target Size (Minimum)
origin-url: https://www.w3.org/TR/WCAG22/#target-size-minimum
edition: 2023-10-05
license: CC0-1.0
converter: hand-authored
content-hash: sha256:1f599bd1416783702977ab18b4e21435141d14d3f6919205425430060c7d5568
status: authoritative
---

# WCAG 2.2 target size: the AA minimum vs. the AAA enhanced target

This is a hand-authored distillation for grounding UI specs — **not** a verbatim copy of the WCAG
text. Read the normative wording at the `origin-url` before quoting it.

## SC 2.5.8 Target Size (Minimum) — Level AA, new in WCAG 2.2

WCAG 2.2 became a W3C Recommendation on **5 October 2023**. It introduced **SC 2.5.8 Target Size
(Minimum)** at **Level AA**, requiring that the target for a pointer input be at least
**24×24 CSS pixels**, except where one of these holds:

- **Spacing** — the target is under 24×24, but a 24-CSS-pixel-diameter circle centred on it does not
  intersect any other target (or the circle of another target); i.e. there is enough spacing that an
  imprecise tap won't hit a neighbour.
- **Inline** — the target is in a sentence, or its size is otherwise constrained by the line-height
  of non-target text.
- **User-agent control** — the size of the target is determined by the user agent and is not modified
  by the author.
- **Essential** — a particular presentation of the target is essential, or is legally required, for
  the information being conveyed.

## SC 2.5.5 Target Size (Enhanced) — Level AAA, from WCAG 2.1

Contrast this with the older **SC 2.5.5 Target Size (Enhanced)**, which shipped in **WCAG 2.1 (2018)**
at **Level AAA** and requires **44×44 CSS pixels**. That is a strictly higher bar, and — being AAA —
was never a general conformance floor.

## Common confusion

**44×44 is not the AA minimum.** 44×44 CSS px is the figure in Apple's Human Interface Guidelines and
is the WCAG **AAA / Enhanced** target (SC 2.5.5). The minimum-target floor introduced at **Level AA**
in WCAG 2.2 is **24×24** (SC 2.5.8). Reaching for 44 by reflex over-shoots the AA requirement and
quietly conflates an AAA criterion with the AA baseline.

Cite as `KB-001@2023-10-05`.
