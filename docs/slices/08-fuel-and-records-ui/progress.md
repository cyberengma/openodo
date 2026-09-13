# Progress — 08-fuel-and-records-ui

## Rulings

1. Records navigation opens a unified timeline; a segmented control switches
   between fuel/charging entries and expense records without changing the
   active vehicle.
2. Fuel and expense forms use the active vehicle's units and currency at the
   time of editing; canonical values are produced by core/domain conversion
   APIs before repository writes.
3. A saved expense matching an active reminder shows explicit reset feedback;
   reminder engine behavior remains in `core` and is not duplicated in UI.

## Session plan

Session 1: fuel/charging form, history, and repository-backed records shell.
Session 2: expense form/detail, validation states, reset feedback, tests, and
acceptance.

Implementation has started locally; no dependencies are added.

2026-09-13, local session complete:
- Added repository-backed Records shell with Fuel/Expenses modes.
- Added liquid/electric fuel form with active vehicle units/currency and
  validation for measurement/cost.
- Added expense form with category selection, title, cost, and validation.
- Added empty states and repository-backed timeline cards.
- Full offline gate passes; detailed feature screens remain intentionally
  scoped for further refinement in this slice's UI iterations.
