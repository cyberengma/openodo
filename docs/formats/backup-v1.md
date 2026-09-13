# Backup JSON v1

The backup root has `schemaVersion: 1`, `exportedAt`, `appVersion`, and
arrays named `vehicles`, `recordTypes`, `fuelEntries`, `expenseRecords`,
`reminders`, and `photos`.

Canonical fields are numeric storage values: metres, millilitres,
watt-hours, and currency minor units. Dates are ISO-8601 `yyyy-MM-dd`
strings. `photos` contains filenames only; binary photo and zip handling is
an app-layer responsibility for slice 10.

Readers ignore unknown fields and default omitted arrays to empty arrays.
Schema versions other than 1 are rejected as typed errors.
