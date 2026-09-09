# Slice 02 — domain-units-money

Written against architecture v0.1. The architecture wins on conflict; gaps
become questions in progress.md.

Builds on slice 01 per its acceptance items: two-module project, all
dependencies locked, gate image available, `core` tests run offline.

## Goal

The `core` module gains its domain vocabulary — vehicles, fuel entries,
expense records, record types, reminders — expressed in canonical units
with exact money arithmetic and a validation layer that returns typed
results. Every later engine (fuel, reminders, portability) composes these
types, so they must be complete, immutable, and thoroughly tested here.

## Scope: In

1. `core.units`: `Metres`, `Millilitres`, `WattHours` as `@JvmInline
   value class` over `Long`; conversion functions to/from km, mi, L,
   US gal, UK gal, kWh using explicit rounding (`RoundingMode.HALF_EVEN`
   on `BigDecimal`), never `Double` arithmetic on the stored value.
   `DistanceUnit`, `VolumeUnit`, `EnergyUnit` enums for display.
2. `core.money`: `Money(minor: Long, currency: String)` with `plus`,
   `minus`, `times(Int)`, `sum(List)`; `UnitPrice(milli: Long,
   currency)`; `UnitPrice.total(volume: Millilitres): Money` with defined
   rounding; a `CurrencyMinorDigits` table (default 2; JPY/KRW 0; KWD/BHD
   3 — extendable) used only for formatting. Mixed-currency ops return a
   typed error, never throw.
3. `core.model`: `Vehicle` (id, name, make, model, year, vin, photo file
   name, `distanceUnit`, `volumeUnit`, `energyUnit`, `currency`,
   `manualOdometer: Metres?`, `manualOdometerAt: Long?`, `isArchived`,
   notes, audit stamps), `FuelEntry` (vehicleId, date, odometer, `kind ∈
   {LIQUID, ELECTRIC}`, volume **or** energy, `unitPrice`, `totalCost`,
   `fuelLabel` string, `fullTank`, `missedPreviousFillUp`, receipt file
   name, notes, audit stamps), `RecordCategory`, `RecordType`,
   `ExpenseRecord` (vehicleId, typeId, date, odometer, title, description,
   cost, `performedBy ∈ {SELF, SHOP}`, shopName, `warrantyUntil:
   LocalDate?`, receipt file name, notes, audit stamps), `Reminder`
   (vehicleId, typeId, `intervalDistance: Metres?`, `intervalMonths:
   Int?`, `anchorDate`, `anchorOdometer`, `active`). All `data class`,
   ids are `Long` with `0` = unsaved.
4. `core.model.DefaultRecordTypes`: the seed list — SERVICE: oil change,
   oil filter, air filter, cabin filter, brake pads, brake fluid, tire
   rotation, wheel alignment, coolant, spark plugs, transmission fluid,
   timing belt, battery, general inspection; REPAIR: engine, transmission,
   electrical, body, suspension, exhaust, cooling, other; UPGRADE: wheels
   & tires, suspension, exhaust, brakes, audio, lighting, body kit,
   performance, other; OTHER: insurance, registration, inspection fee,
   parking, tolls, fine, wash, accessories, other.
5. `core.validation`: `ValidationResult` sealed type (`Ok`, `Warning`,
   `Error`) with error codes; validators for `FuelEntry` (volume/energy
   > 0, cost ≥ 0, exactly one of volume/energy set per `kind`, date not
   in the future relative to injected `Clock`), `ExpenseRecord` (cost ≥
   0), `Reminder` (at least one interval set, positive), and
   `OdometerCheck(previousMax: Metres?, proposed: Metres)` returning a
   `Warning(ODOMETER_DECREASED)` rather than an error.
6. `core.model.CurrentOdometer.of(vehicle, allRecordOdometersWithDates)`
   implementing the architecture rule (max over records unless the
   manual override is newer than every record).
7. Tests for every public function, including rounding edge cases
   (0.5 mL, negative distances rejected, JPY totals, gal↔L round trips
   within 1 mL over 10 000 random values from a fixed-seed
   `kotlin.random.Random(42)`).

## Scope: Out

- Fuel consumption maths, reminder due logic, statistics, serialization
  (slices 03–05).
- Anything in `app`.
- i18n of type names — names are English keys; localization is a UI
  concern later.

## Open questions (propose-first)

1. Odometer for electric-only vehicles that report distance in tenths:
   is `Metres` precision enough, or does any importer (Drivvo/Fuelio)
   supply finer data? Propose after checking the fixture files' column
   precision; default recommendation is `Metres`.
2. Whether `FuelEntry` should carry a `stationName` field now (Drivvo
   exports include it). Propose; recommendation is yes, nullable.

## Acceptance

- Gate command exits 0 from a clean clone.
- `core/src/test` contains ≥ 40 test cases across `units`, `money`,
  `model`, `validation`; handoff lists the test class names and counts.
- Red proof: `OdometerCheck` and the mixed-currency error each shown
  failing against a deliberately wrong assertion before passing.
- `grep -rn "Double" core/src/main` returns only display-conversion
  helpers, none in model fields; handoff shows the grep output.
- No Android or DI import appears in `core/src/main`
  (`grep -rln "^import android\|^import dagger\|^import javax.inject"
  core/src/main` prints nothing).

## Session plan

- Session 1: items 1, 2, 7 (units + money tests).
- Session 2: items 3–6 and remaining tests; answer open questions in
  progress.md before adding `stationName`.
