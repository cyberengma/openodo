# Handoff — 03-fuel-engine, session 1 (single local session)

Builder: operator, manual local session; no new dependencies or remote
server activity. The existing locked catalog was unchanged.

## Rulings

1. A first-ever entry opens history but never closes a span and does not
   imply a missed fill. The opener's own missed flag invalidates the span it
   closes, while a closing entry with that flag is invalid.
2. The default trend moving-average window is 5; callers can provide another
   positive window.
3. Mixed currencies return typed `SpanBuildResult.MixedCurrency` or
   `FuelStatsResult.Error`; no expected currency failure throws.

## Implementation

- `SpanBuilder` sorts by date, odometer, and id; keeps liquid and electric
  histories separate; accumulates partial and closing fills; emits invalid
  spans with `MISSED_FILL_UP`, `NEGATIVE_DISTANCE`, or `ZERO_VOLUME`.
- `ConsumptionSpan` stores canonical metres, millilitres, watt-hours,
  integer `mlPer100km` or `whPerKm`, cost, dates, ids, validity, and reason.
- `Economy` provides L/100km, km/L, US MPG, UK MPG, kWh/100km, and mi/kWh
  conversions with caller-selected scale and HALF_EVEN rounding.
- `FuelStats` provides totals, volume-weighted average consumption,
  best/worst spans, milli-minor cost per km, last fill-up, distance since
  last fill-up, and inclusive monthly buckets.
- `TrendSeries` produces chronological valid-span points and a moving
  average. `PriceStats` groups min/max/average unit prices by fuel label and
  supports an inclusive date range.

## Worked example

Six liquid entries:

| Entry | Odometer | Volume | Full tank |
|---|---:|---:|---|
| 1 | 10,000 km | 50,000 mL | yes |
| 2 | 10,300 km | 30,000 mL | no |
| 3 | 10,500 km | 20,000 mL | no |
| 4 | 11,000 km | 40,000 mL | yes |
| 5 | 11,400 km | 25,000 mL | no |
| 6 | 12,000 km | 45,000 mL | yes |

The opener's volume is excluded from each consumption span. Therefore:

- Span 1 → 4: distance = 1,000 km; volume = 30,000 + 20,000 + 40,000 =
  90,000 mL; consumption = 90,000 / 1,000 × 100 = 9,000 mL/100km =
  9.000 L/100km.
- Span 4 → 6: distance = 1,000 km; volume = 25,000 + 45,000 = 70,000
  mL; consumption = 7,000 mL/100km = 7.000 L/100km.
- Combined volume-weighted result: 160,000 mL / 2,000 km × 100 = 8,000
  mL/100km = 8.000 L/100km.

The worked-example test matches the first two canonical values. The stats
tests cover the weighted aggregate.

## Tests and results

Fuel package: 41 tests, all passing.

- `SpanBuilderTest`: 19 tests covering empty/single/full-tank spans,
  partials, missed flags, zero volume, negative distance, ordering, PHEV
  separation, electric consumption, typed currency errors, ids/dates,
  completed-only spans, the six-entry example, and 10,001-entry performance.
- `EconomyTest`: 8 tests covering all six conversions, scale 1 and 2 for
  5.555 L/100km, zero denominators, and caller scale behavior.
- `FuelStatsTest`: 8 tests covering empty input, totals, weighted average,
  best/worst, cost per km, last fill-up, distance, month ranges, and mixed
  currency errors.
- `TrendSeriesTest`: 5 tests covering chronological ordering, moving
  averages, invalid-span filtering, the default window of 5, and invalid
  windows.
- `PriceStatsTest`: 4 tests covering label grouping, date ranges, missing
  prices, and empty input.

The synthetic performance test logged:

```
10,001 fuel entries processed in 41ms
```

Required red proofs were captured before the final green run:

```
SpanBuilderTest > partial fills are included in span FAILED
    java.lang.AssertionError: expected:<40000> but was:<50000>

SpanBuilderTest > missed fill in middle invalidates span FAILED
    java.lang.AssertionError
```

Final local gate:

```
./gradlew --offline --no-daemon :core:test :app:testDebugUnitTest
BUILD SUCCESSFUL in 27s
EXIT=0
```

Acceptance checks:

```
```
