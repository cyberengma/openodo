# RC-4 — Test and Stability Hardening

## Goal

Prove the completed app works from a clean local checkout and is stable
across realistic lifecycle and data conditions.

## Scope

- Repository integration tests for all entity types and nullable fields.
- ViewModel/state tests for vehicle switching, forms, reset, import, and
  restore.
- Compose tests for first launch, add/edit/delete, quick actions, reminders,
  portability, and error states.
- Process recreation and configuration-change checks.
- Empty database startup checks.
- Multiple vehicles with different units/currencies.
- Real Drivvo/Fuelio fixture regression tests.
- Receipt permission/failure tests.
- WorkManager daily scheduling and idempotence tests using an approved test
  setup; add a dependency only through a recorded ruling if required.
- Crash/exception review and strict offline gate from a clean clone.

## Acceptance

- Full test suite is green offline.
- No known crash exists in first launch, vehicle switching, forms, restore,
  import, or reminder scheduling.
- Tests prove data from one vehicle cannot appear with another vehicle's
  formatting or query scope.
- Handoff lists test commands, counts, and remaining risks.
