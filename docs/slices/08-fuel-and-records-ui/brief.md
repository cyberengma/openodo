# Slice 08 — fuel-and-records-ui

Builds on Slice 07, `core.fuel`, `core.model`, `core.portability`, and the
approved UI authority.

## Goal

Implement fuel/charging entry, fuel history/detail, expense record entry, and
the unified records list/detail using repositories and pure core engines.

## Scope: In

1. Liquid and electric fuel forms with active vehicle units/currency,
   validation, full/partial controls, missed-fill flag, receipt attachment,
   and odometer warning.
2. Fuel history/detail with filters, month grouping, economy, cost, edit,
   delete, and incomplete/import-origin indicators.
3. Expense record form with all four categories, type selection, Self/Shop,
   shop, warranty, receipt, notes, validation, and reminder-reset feedback.
4. Unified records list/detail with filtering, search, edit, delete, and
   empty/error states.
5. UI tests for valid/invalid forms, vehicle-scoped formatting, receipt
   failure, unsaved changes, and destructive confirmation.

## Scope: Out

- Reminder list implementation, statistics charts, backup/import screens.

## Acceptance

- Forms never perform domain calculations themselves; they call `core`.
- Liquid/electric data cannot be mixed accidentally.
- Active vehicle configuration is used in every field and row.
- Matching expense save visibly explains reminder reset.
- Offline gate and UI tests pass.
