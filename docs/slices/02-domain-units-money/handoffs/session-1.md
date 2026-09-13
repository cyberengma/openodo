# Handoff — 02-domain-units-money, session 1 (single local session)

Builder: operator, manual local session (offline; catalog unchanged from
slice 01).

## What landed in `core`

- `core.units`: `DistanceUnit` / `VolumeUnit` / `EnergyUnit` enums;
  `Millilitres`, `WattHours`; `Metres` extended (`to(unit)`, `miles()`).
  All display conversions are BigDecimal at scale 3, RoundingMode.HALF_EVEN;
  exact constants (1609344 mm/mi, 3785.411784 mL/USgal, 4546.09 mL/UKgal).
  Storage stays canonical Long (architecture rule untouched).
- `core.money`: `Money(minor, currency)` (+/−/times, companion `sum`),
  `sealed MoneyResult { Success, MixedCurrency(left,right) }` — mixed
  currency returns a typed value, never throws; `UnitPrice.total(volume)`
  rounds to the currency's minor digits HALF_EVEN;
  `CurrencyMinorDigits` table (default 2; JPY/KRW 0; KWD/BHD 3, extensible).
- `core.model`: `Vehicle`, `FuelEntry` (+ nullable `stationName`),
  `ExpenseRecord`, `Reminder`, `RecordType`, enums, 40-entry
  `DefaultRecordTypes.all`, and `CurrentOdometer.of(vehicle, records)` —
  records-max unless the manual override's whole UTC day is strictly newer
  than every record day; `Empty` result when nothing exists; an override
  with a null stamp is inert.
- `core.validation`: `sealed ValidationResult { Ok, Warning(codes,msg),
  Error(codes,msg) }`, `ValidationErrorCode`, `Validators.fuelEntry(entry,
  clock)` / `.expenseRecord(record)` / `.reminder(reminder)` /
  `.odometer(previous, proposed)` — decrease ⇒ Warning(ODOMETER_DECREASED).
  All expected failures return typed values; nothing throws.

## Commands + results

RED (two deliberately wrong assertions planted, fixed afterwards):

```
> Task :core:test FAILED
MoneyTest > mixed currency plus returns typed error FAILED
    java.lang.AssertionError: expected typed error, got MixedCurrency(left=EUR, right=USD)
OdometerCheckTest > decreased odometer is a warning FAILED
    java.lang.AssertionError: expected Ok, got Warning(codes=[ODOMETER_DECREASED], ...)
```

GREEN (the exact gate command, offline):

```
./gradlew --offline --no-daemon :core:test :app:testDebugUnitTest
BUILD SUCCESSFUL in 23s
EXIT=0
```

76 test cases, 0 failures / 0 errors:

```
core.units.MetresTest             7
core.units.MillilitresTest        9   (incl. 10k-value Random(42) gal/L round trips)
core.units.WattHoursTest          4   (incl. 10k-value Random(42) kWh round trips)
core.money.MoneyTest              9   (incl. mixed → MoneyResult.MixedCurrency)
core.money.UnitPriceTest          5   (CAD / EUR / JPY / KWD totals)
core.money.CurrencyMinorDigitsTest 2
core.model.ModelsTest             3
core.model.DefaultRecordTypesTest 3   (counts 14/8/9/9, all defaults, unique names)
core.model.CurrentOdometerTest    7   (rule incl. tie, inert stamp, empty, lone override)
core.validation.FuelEntryValidatorTest      12 (incl. every code + combined codes)
core.validation.ExpenseRecordValidatorTest   3
core.validation.ReminderValidatorTest        7
core.validation.OdometerCheckTest            5 (decrease → Warning, not error)
```

## Acceptance greps

```
$ grep -rn "Double" core/src/main          → (nothing) rc=1
$ grep -rln "^import android\|^import dagger\|^import javax.inject" core/src/main → (nothing) rc=1
```
