# Slice 09 — reminders-ui

Builds on Slice 07, Slice 04 reminder engines, and the approved reminder
design authority.

## Goal

Implement reminder list, detail, creation/editing, manual reset, and the
local daily WorkManager notification check.

## Scope: In

1. Render OK, Due Soon, Overdue, Inactive, and Unanchored states with text,
   icons, order, and restrained colors; never color alone.
2. Render date/distance intervals, next due values, remaining values, trigger
   axis, stale odometer metadata, and active vehicle formatting.
3. Implement creation/editing with one or both positive intervals, active
   state, no-anchor behavior, manual reset, deactivate, and delete.
4. Connect matching expense reset and deleted-record re-anchoring feedback.
5. Add WorkManager daily local notification check; no network permission or
   remote notification service.
6. Add UI and worker tests for all statuses, threshold boundaries, stale
   odometer, reset, deletion, inactive, and unanchored states.

## Scope: Out

- Predictive notifications, cloud reminders, recurrence analytics.

## Acceptance

- Reminder state is calculated by `core.reminders`, not duplicated in UI.
- Notification scheduling remains local and deterministic.
- Offline gate and UI/worker tests pass.
