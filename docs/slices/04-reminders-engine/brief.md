# Slice 04 — reminders-engine

Written against architecture v0.1. The architecture wins on conflict; gaps
become questions in progress.md.

Builds on slice 02 per its acceptance items: `Reminder`, `ExpenseRecord`,
`RecordType`, `CurrentOdometer`, validation. Independent of slice 03.

## Goal

A pure `core.reminders` library that, given a reminder, the vehicle's
current odometer, and today's date, produces a deterministic status with
the next-due date and odometer — and a resolver that applies the
auto-reset rule when an expense record is logged. This is the product's
headline feature; the UI and the WorkManager job in later slices call
exactly these functions and nothing else.

## Scope: In

1. `core.reminders.DueCalculator.evaluate(reminder, currentOdometer:
   Metres?, today: LocalDate): ReminderStatus` where `ReminderStatus`
   carries `state ∈ {OK, DUE_SOON, OVERDUE, INACTIVE, UNANCHORED}`,
   `nextDueDate: LocalDate?`, `nextDueOdometer: Metres?`,
   `remainingDays: Long?`, `remainingDistance: Metres?`, and
   `triggeredBy ∈ {DATE, DISTANCE, BOTH, NONE}`.
2. Month arithmetic via `LocalDate.plusMonths` (Jan 31 + 1 month = Feb
   28/29 — accept `java.time` semantics, document it in KDoc).
3. `DUE_SOON` thresholds per architecture: for the date axis,
   `min(10 % of interval in days, 30 days)`; for the distance axis,
   `min(10 % of interval, 500 km)`. When both axes exist, the worse
   state wins; `triggeredBy` reports which.
4. `UNANCHORED` when the reminder has no anchor yet (created before any
   matching record); the calculator does not invent one.
5. `core.reminders.ResetResolver.onRecordLogged(reminders:
   List<Reminder>, record: ExpenseRecord): List<Reminder>` — returns the
   reminders (same vehicle, same `typeId`) with anchor set to the
   record's date and odometer **only if** the record is newer than the
   current anchor by date, or same date and higher odometer; older
   records never move an anchor backwards. Also
   `onRecordDeleted(reminders, deletedRecord, remainingRecordsOfType)`
   re-anchors to the newest remaining record of that type, or clears the
   anchor if none.
6. `core.reminders.ManualReset.apply(reminder, date, odometer)`.
7. `core.reminders.ReminderSummary.forVehicle(reminders,
   currentOdometer, today)`: counts per state, sorted list by urgency
   (overdue by largest margin first, then due-soon by smallest
   remaining, then OK by next due).
8. Tests with an injected fixed `today`, covering: date-only, distance-
   only, both (each axis triggering first), boundary days (due today,
   due tomorrow, one day overdue), leap-year February, threshold cap
   (12-month interval → 30-day window, not 36), 60 000 km interval →
   500 km window, unanchored, inactive, no current odometer, reset by
   newer/older/same-day records, deletion re-anchoring, and summary
   ordering with ties.

## Scope: Out

- Notification scheduling, WorkManager, any Android API.
- Reminders keyed to fuel entries or to free-text tasks (post-MVP).
- Recurrence history / "was done on time" analytics.

## Open questions (propose-first)

1. Should a reminder with both intervals be allowed to set only one
   threshold style ("whichever first" is the only mode in v1)? Propose;
   recommendation: single mode in v1, document as ADR candidate.
2. Behaviour when `currentOdometer` is older than 90 days: still compute
   distance state, or flag `STALE_ODOMETER` in the status? Propose;
   recommendation: add a `staleOdometer: Boolean` field, no new state.

## Acceptance

- Gate command exits 0 from a clean clone.
- `core/src/test/.../reminders/` contains ≥ 35 test cases; handoff lists
  them one line each.
- Red proof: the "older record must not move the anchor backwards" test
  and the 30-day threshold-cap test each shown failing against a
  deliberately wrong expectation before passing.
- Handoff contains a truth table (≥ 8 rows) of `(anchor, interval,
  today, odometer) → state/triggeredBy` matched by tests.

## Session plan

- Session 1: items 1–4, 8 (calculator tests).
- Session 2: items 5–7, remaining tests, truth table.
