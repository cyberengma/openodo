# Slice 06 — persistence

Written against architecture v0.1 and the approved UI authority in
`docs/design/`. Architecture wins on conflict; gaps become questions in
`progress.md`.

## Goal

Persist the complete domain locally with Room schema v1 and repositories that
map database entities to the existing pure `core` models without leaking
Android types into `core`.

## Scope: In

1. Add Room entities for `Vehicle`, `RecordType`, `FuelEntry`,
   `ExpenseRecord`, and `Reminder`; use canonical Long fields for metres,
   millilitres, watt-hours, minor money, and milli-unit prices.
2. Persist each vehicle's distance unit, volume unit, energy unit, and
   currency. Never make these global preferences.
3. Add DAOs with observable list/detail queries, vehicle filtering, active
   vehicle behavior, and reminder/record queries needed by the UI authority.
4. Add Room database schema v1 and export it to `app/schemas/`.
5. Add repositories mapping entities to/from `core.model`, preserving
   nullable fields, ISO `LocalDate`, epoch audit timestamps, and typed domain
   validation boundaries.
6. Add Hilt database/repository wiring in `app`; no repository logic belongs
   in the UI.
7. Add Robolectric tests with `robolectric.offline=true` for entity mapping,
   CRUD, vehicle-scoped configuration, queries, and schema export.

## Scope: Out

- Compose screens, navigation, WorkManager, notifications.
- Backup/restore file I/O and SAF.
- Import merging or deduplication.
- New domain rules or new dependencies.

## Open questions

1. Whether Room stores dates as ISO strings or epoch days. Recommendation:
   ISO strings to preserve the architecture's zone-free LocalDate rule.
2. Whether archived vehicles remain queryable in the primary DAO. Recommend
   explicit active/archived queries; never delete them implicitly.

## Acceptance

- Offline gate passes from a clean checkout.
- Schema v1 is exported and committed.
- Vehicle-scoped units/currency round-trip without mutation.
- Repository tests cover every entity and nullable field.
- Robolectric runs offline and core remains Android/DI-free.
- Hilt wiring is tested through database/repository construction.
- No `Double`, network permission, signing config, or new dependency.
