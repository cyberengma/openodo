# Handoff — 04-reminders-engine, session 1 (single local session)

Builder: operator, manual local session; no new dependencies and no remote
server activity.

## Rulings

1. Both reminder intervals use one v1 mode: whichever axis elapses first
   determines the status. Alternative threshold modes are a future ADR.
2. A current odometer older than 90 days still participates in distance
   calculations. `ReminderStatus.staleOdometer` reports the age without
   adding a new state.

## Implementation

- `DueCalculator.evaluate` handles `OK`, `DUE_SOON`, `OVERDUE`,
  `INACTIVE`, and `UNANCHORED`, exposes next date/odometer, remaining
  values, trigger axis, and stale-odometer state.
- Date arithmetic uses `LocalDate.plusMonths`; January 31 behavior follows
  java.time semantics.
- Date DUE_SOON is the smaller of 10% of the actual date interval and 30
  days. Distance DUE_SOON is the smaller of 10% of the interval and 500 km.
- `ResetResolver.onRecordLogged` resets only matching vehicle/type reminders
  when the record is newer by date, or same-date with a higher odometer.
  Older records never move an anchor backward.
- `onRecordDeleted` reanchors to the newest remaining matching record or
  clears the anchor. `ManualReset.apply` replaces both anchor values.
- `ReminderSummary` counts every state and sorts overdue before due-soon,
  then OK/unanchored/inactive with deterministic remaining-value and id ties.

## Truth table

| Anchor / interval | Today / odometer | State | Trigger |
|---|---|---|---|
| none / 1 month | any / any | UNANCHORED | NONE |
| inactive / any | any / any | INACTIVE | NONE |
| June 1 / 1 month | June 15 / no odometer | OK | NONE |
| May 18 / 1 month | June 15 / no odometer | DUE_SOON | NONE |
| May 15 / 1 month | June 15 / no odometer | OVERDUE | DATE |
| Jan 31 / 1 month | Feb 28, leap year | DUE_SOON | NONE |
| odometer 100,000 km / 10,000 km | 100,100 km | OK | NONE |
| odometer 100,000 km / 10,000 km | 110,000 km | OVERDUE | DISTANCE |
| odometer 100,000 km / 60,000 km | 159,500 km | DUE_SOON | NONE |
| both axes, date due first | date due / distance below due | OVERDUE | DATE |
| both axes, distance due first | date below due / distance due | OVERDUE | DISTANCE |
| both axes due together | both thresholds reached | OVERDUE | BOTH |

## Tests and red proofs

Reminder package: 40 tests, all passing; 160 total core tests.

- `DueCalculatorTest`: 22 tests covering inactive/unanchored, date-only,
  distance-only, both-axis precedence, due boundaries, leap arithmetic,
  threshold caps, stale odometers, missing current odometer, and exposed
  next-due values.
- `ResetResolverTest`: 10 tests covering newer/older/same-day records,
  vehicle/type filtering, unanchored reset, deletion reanchoring, and
  clearing.
- `ManualResetTest`: 2 tests covering anchor replacement and preserved
  configuration.
- `ReminderSummaryTest`: 6 tests covering counts, urgency ordering, ties,
  empty input, and stale context.

Required red proofs were captured before correction:

```
DueCalculatorTest > twelve months caps date threshold at thirty days FAILED
    java.lang.AssertionError: expected:<OK> but was:<DUE_SOON>

ResetResolverTest > older record does not move anchor backward FAILED
    java.lang.AssertionError: expected:<2026-06-14> but was:<2026-06-15>
```

Final gate:

```
./gradlew --offline --no-daemon :core:test :app:testDebugUnitTest
BUILD SUCCESSFUL in 31s
EXIT=0
```

Purity checks passed: no `Double`, Android, Dagger, or javax.inject imports
in `core/src/main`.
