# Slice 03 — fuel-engine

Written against architecture v0.1. The architecture wins on conflict; gaps
become questions in progress.md.

Builds on slice 02 per its acceptance items: `core.units`, `core.money`,
`core.model.FuelEntry`, validation.

## Goal

A pure function library in `core.fuel` that turns a vehicle's fuel history
into consumption figures, per-entry economy, cost-per-distance, and trend
series — correctly handling partial fills, missed fill-ups, unit changes,
and electric entries — so the dashboard and statistics screens only ever
format numbers, never compute them.

## Scope: In

1. `core.fuel.SpanBuilder.build(entries: List<FuelEntry>):
   List<ConsumptionSpan>` — sort by (date, odometer, id); a span runs from
   one `fullTank` entry to the next; partial entries between them
   contribute volume/energy; a span containing any entry with
   `missedPreviousFillUp = true` (including the closing entry) is emitted
   with `valid = false` and a reason code; spans with non-positive
   distance or zero volume are `valid = false`. Liquid and electric spans
   are separate sequences (a PHEV has both).
2. `ConsumptionSpan` fields: start/end entry ids, `distance: Metres`,
   `volume: Millilitres?`, `energy: WattHours?`, `cost: Money`,
   `mlPer100km: Long?` / `whPerKm: Long?` (canonical), `valid`, `reason`.
3. `core.fuel.Economy` display conversions from canonical: L/100km,
   km/L, MPG (US), MPG (UK), kWh/100km, mi/kWh — all `BigDecimal`
   HALF_EVEN to a caller-supplied scale.
4. `core.fuel.FuelStats.of(entries, clock)`: total volume, total cost,
   average consumption (volume-weighted over valid spans, not mean of
   means), best/worst valid span, cost per km (canonical minor-per-km
   ×1000), last fill-up, distance since last fill-up, month buckets of
   cost and volume as `List<MonthBucket(YearMonth, Money, Millilitres?,
   WattHours?)>` for a given range.
5. `core.fuel.TrendSeries.consumption(spans, window)`: chronological
   points `(endDate, canonical value)` plus a simple moving average over
   `window` valid spans.
6. `core.fuel.PriceStats`: min/max/average unit price per fuel label
   over a range.
7. Fixture-free tests built in memory covering: single full tank (no
   span), two full tanks, partials between fulls, missed fill-up in the
   middle and at the span end, zero distance, out-of-order insertion
   dates, odometer that goes backward (span invalid, reason
   `NEGATIVE_DISTANCE`), PHEV interleaving, rounding of 5.555 L/100km at
   scale 1 and 2, and a 10 000-entry synthetic list finishing under 200 ms
   (assert only on correctness; log the timing).

## Scope: Out

- Persistence, charts, and any UI.
- Currency conversion (mixed currencies in one vehicle return a typed
  error from `Money.sum`, propagated as `FuelStats.Error`).
- Predictive range / "km until empty" — post-MVP.

## Open questions (propose-first)

1. Whether a span opened by the very first entry ever logged should be
   treated as `missedPreviousFillUp` implicitly (Fuelio does; Drivvo
   doesn't). Propose; recommendation: the first entry never closes a
   span and the first *closing* entry is valid only if its own flag is
   false.
2. Moving-average default window (3 vs 5). Propose; recommendation 5.

## Acceptance

- Gate command exits 0 from a clean clone.
- `core/src/test/.../fuel/` contains ≥ 30 test cases; handoff lists
  them with the scenario each covers (one line each).
- Red proof: the partial-fill accumulation test and the missed-fill
  exclusion test each shown failing against a deliberately wrong
  expected value before passing.
- Handoff includes a worked example table: 6 hand-crafted entries → the
  spans produced → the L/100km figures, computed by hand in the handoff
  and matched by the test.

## Session plan

- Session 1: items 1–3, tests for spans and conversions.
- Session 2: items 4–7, remaining tests, worked example.
