# Progress — 03-fuel-engine

## Rulings

1. A first-ever entry does not imply a missed fill-up. It opens a span,
   but never closes one; the first closing full-tank entry is valid when its
   own `missedPreviousFillUp` flag is false. A missed flag on any entry in a
   span invalidates that span.
2. The moving-average default window is 5. Callers may pass another positive
   window explicitly.
3. Mixed currency is represented as a typed `SpanBuildResult.MixedCurrency`
   or `FuelStats.Error`; no expected currency failure throws across the core
   boundary.

## Session plan

Session 1: span builder, canonical economy conversions, and tests.
Session 2: statistics, trends, price analysis, worked example, and tests.

Implementation has started locally; no dependencies are added.

2026-09-13, local session complete:
- 41 fuel tests and 120 total core tests pass.
- Offline gate passes: `./gradlew --offline --no-daemon :core:test
  :app:testDebugUnitTest`.
- Purity checks pass: no `Double` and no Android/DI imports in `core`.
- Synthetic 10,001-entry history completed in 41ms.
- Required red proofs were captured for partial-fill accumulation and
  missed-fill exclusion, then corrected.
