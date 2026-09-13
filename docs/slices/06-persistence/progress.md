# Progress — 06-persistence

## Rulings

1. Room stores `LocalDate` values as ISO-8601 strings. This preserves the
   zone-free domain date rule and avoids timezone conversion.
2. Archived vehicles remain queryable through explicit active/all/archived
   DAO methods. No operation implicitly deletes archived vehicles.

## Session plan

Session 1: Room entities, schema, DAOs, database, and core mappers.
Session 2: repositories, Hilt wiring, Robolectric tests, and acceptance.

Implementation has started locally; no dependencies are added.

2026-09-13, local session complete:
- Room schema v1, entities, DAOs, database, repositories, mappers, and Hilt
  wiring implemented.
- Vehicle-scoped units/currency round-trip without canonical-value mutation.
- Explicit active/archived vehicle queries and no implicit deletion.
- Robolectric persistence tests pass offline; full gate passes.
- Schema committed at `app/schemas/ca.terradevop.openodo.data.local.OpenOdoDatabase/1.json`.
