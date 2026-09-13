# Handoff — 06-persistence, session 1

Builder: operator, manual local session; no dependencies added.

## Rulings

1. Room stores `LocalDate` as ISO strings, preserving zone-free domain dates.
2. Archived vehicles remain queryable through explicit active/archived/all
   DAO queries; no operation implicitly deletes them.

## Implementation

- Added Room schema v1 entities for vehicles, record types, fuel entries,
  expense records, and reminders.
- Canonical values remain Long fields: metres, millilitres, watt-hours,
  minor currency units, and milli-unit prices.
- Vehicle distance unit, volume unit, energy unit, and currency are persisted
  per vehicle.
- Added explicit DAO queries for active, archived, all vehicles, vehicle
  history, active reminders, and matching expense records.
- Added core mappers preserving ISO dates, nullable values, audit timestamps,
  value-class fields, money, and unit prices.
- Added repositories so UI code does not access Room directly.
- Added Hilt providers for database, DAOs, and repositories.

## Tests

`RoomPersistenceTest` covers:

- Full vehicle field round-trip.
- Vehicle-scoped miles/gallons/USD configuration.
- Active, archived, and all-vehicle queries.
- Changing display configuration without changing canonical odometer.
- Explicit deletion.

`RepositoryConstructionTest` verifies database/repository construction through
Robolectric application context.

Room tests use the Robolectric runner with `sdk = 35` and remain offline.

## Schema

Generated and committed:

```text
app/schemas/ca.terradevop.openodo.data.local.OpenOdoDatabase/1.json
```

## Gate

```text
./gradlew --offline --no-daemon :core:test :app:testDebugUnitTest
BUILD SUCCESSFUL in 21s
EXIT=0
```
