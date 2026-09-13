# RC-1 — Functional Completion

## Goal

Turn the current installable MVP shell into a coherent end-to-end local app
for vehicles, fuel, expenses, reminders, and dashboard use.

## Scope

- Replace prototype forms with proper date, odometer, unit, currency, notes,
  station/shop, warranty, full/partial, and receipt fields.
- Use `core.validation` for form validation; do not duplicate domain rules in
  composables.
- Add edit and delete flows for vehicles, fuel entries, expenses, and
  reminders with confirmations and unsaved-change handling.
- Persist and restore the active vehicle across app restarts.
- Ensure all display values use the active vehicle's configuration.
- Show dashboard recent activity, active reminder summaries, stale odometer,
  and useful empty/loading/error states.
- Wire matching expense saves through `ResetResolver` and show reset feedback.
- Wire deleted expense records through re-anchoring behavior.
- Implement receipt attachment as a local URI/filename flow; no upload.
- Wire real statistics data from `FuelStats`, `TrendSeries`, and `PriceStats`.

## Acceptance

- A clean install can create a vehicle, configure units/currency, add fuel,
  add an expense, create a named reminder, and observe the reminder reset
  after matching work.
- Invalid fuel and expense data blocks save with field-level messages.
- Odometer decrease is a warning, not a silent failure.
- Edit/delete flows work and preserve vehicle scope.
- Statistics update from persisted records.
- Vehicle configuration survives restart and differs correctly between two
  vehicles.
- Offline gate and UI/state tests pass.
