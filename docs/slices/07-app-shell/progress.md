# Progress — 07-app-shell

## Rulings

1. Phone navigation uses five bottom destinations: Vehicles, Dashboard,
   Records, Reminders, and Settings. Statistics is reached from dashboard
   metrics; Portability is reached from Settings.
2. The active vehicle is held by the app shell and persisted through the
   vehicle repository's selected vehicle state in this first shell. Every
   vehicle-dependent screen formats values from the active vehicle.
3. Vehicle units and currency are edited only in the vehicle form; global
   settings provide an explicit shortcut but no global override.

## Session plan

Session 1: navigation shell, theme, vehicle list/form, and active switching.
Session 2: dashboard states, repository-backed summaries, UI tests, and
acceptance.

Implementation has started locally; no dependencies are added.

2026-09-13, local session complete:
- Implemented Compose app shell with five-item navigation.
- Added repository-backed vehicle list, empty state, vehicle form, active
  switching, archived display, and vehicle-scoped unit/currency labels.
- Added dashboard shell with active vehicle, odometer, quick actions, and
  placeholder sections for later feature slices.
- Added explicit light/dark Material theme tokens and shell state tests.
- Full offline gate passes.
