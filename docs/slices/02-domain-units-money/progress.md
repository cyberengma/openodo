# Progress — 02-domain-units-money

Session plan: Session 1 = items 1, 2, 7 (units + money + their tests);
Session 2 = items 3–6 plus remaining tests.

## Rulings (propose-first questions, answered before implementation)

1. Odometer precision for tenths-reporting EVs: default recommendation is
   `Metres`. Checked `fixtures/imports/` — only `README.md` is present,
   the sample CSVs have not been supplied yet, so column precision is not
   yet checkable. Ruling: `Metres` (a tenth-of-a-km step is 100 m; plenty
   of headroom). Re-check when the fixtures land (slice 05); a finer
   representation would be a deliberate `core.units` change at that point.
2. `stationName` on `FuelEntry` now: yes, per the recommendation —
   `val stationName: String?`, nullable.

2026-09-13, session 1+2 together (operator, manual local builds):
(all results recorded in handoffs/session-1.md at end of work)

2026-09-13, session 1+2 (single local session): complete.
- items 1–7 done: units (enums + BigDecimal HALF_EVEN conversions), money
  (Money/UnitPrice/CurrencyMinorDigits, typed MoneyResult for mixed
  currency), model (all six entities + 40-seed DefaultRecordTypes +
  CurrentOdometer rule), validation (typed ValidationResult + Validators
  with injected Clock), 76 test cases, red proofs for
  OdometerCheck + mixed-currency both shown failing first.
- `./gradlew --offline --no-daemon :core:test :app:testDebugUnitTest`
  EXIT 0; greps clean (no `Double` in main, no android/dagger/inject
  imports in core). Full detail in handoffs/session-1.md.
