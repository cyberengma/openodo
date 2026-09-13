# Progress — 09-reminders-ui

## Rulings

1. Reminder status remains calculated by `core.reminders.DueCalculator`; the
   Compose layer only renders returned status values.
2. The daily check is local WorkManager work with unique name
   `openodo-reminder-check`; no network or remote notification service.
3. The locked catalog has no WorkManager testing artifact, so worker coverage
   verifies deterministic scheduling identity while Robolectric/application
   startup tolerates an unavailable WorkManager test initializer.

## Session plan

Session 1: reminder list/status cards and edit/reset form.
Session 2: local worker scheduling, shell tests, and acceptance.

2026-09-13, local session complete:
- Replaced Reminders placeholder with repository-backed reminder list.
- Added status rendering through `DueCalculator`, manual reset, delete, and
  creation form for date/distance intervals.
- Added deterministic local daily WorkManager worker and unique scheduling.
- Full offline gate passes.
