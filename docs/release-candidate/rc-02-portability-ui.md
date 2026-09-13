# RC-2 — Backup, Import, and Export UI

## Goal

Expose the already-tested `core.portability` functions through safe local
Android file flows.

## Scope

- JSON backup export using SAF create-document.
- JSON backup restore using SAF open-document.
- CSV export for fuel and expenses.
- Drivvo and Fuelio import file selection.
- Preview/confirmation before applying imported data.
- Imported, warning, skipped counts and row-level report details.
- Malformed CSV, unsupported schema, cancellation, permission, and local
  file failure states.
- Preserve every vehicle's units/currency in backup and restore.
- Apply import to a new vehicle/dataset only according to Slice 05 rules;
  no post-MVP deduplication.

## Acceptance

- A domain dataset exports to JSON, restores losslessly, and preserves
  vehicle-scoped display configuration.
- Both real fixtures can be selected through the UI and produce their known
  report summaries.
- Unsupported schema and malformed CSV are actionable local errors.
- No file is uploaded or transmitted.
- Offline gate and portability UI tests pass.
