# RC-2 Progress

Implemented and verified:

- SAF JSON backup export.
- SAF JSON backup restore into Room repositories.
- Fuel CSV export.
- Expense CSV export.
- Drivvo/Fuelio CSV file selection and importer report summary.
- Existing BackupV1, RFC-4180, and fixture importer tests remain green.

The Data destination is now reachable from the app shell and exposes the
working portability actions. Restore is repository-backed and preserves the
canonical/vehicle-scoped model through `BackupV1`.
