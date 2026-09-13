# Progress — 04-reminders-engine

## Rulings

1. Reminders with both intervals use one v1 mode: whichever axis elapses
   first determines the status. Alternative threshold styles are an ADR
   candidate for a later version.
2. Distance status is still computed when the current odometer is older than
   90 days. `ReminderStatus.staleOdometer` records that condition without
   introducing a new state.

## Session plan

Session 1: DueCalculator and calculator tests.
Session 2: reset resolver, manual reset, summary ordering, truth table, and
remaining tests.

Implementation has started locally; no dependencies are added.

2026-09-13, local session complete:
- Implemented DueCalculator, ResetResolver, ManualReset, and
  ReminderSummary.
- 40 reminder tests and 160 total core tests pass.
- Offline gate passes: `./gradlew --offline --no-daemon :core:test
  :app:testDebugUnitTest`.
- Both required red proofs were captured and corrected.
- No `Double` or Android/DI imports occur in `core/src/main`.
