# Handoff — 07-app-shell, session 1

Builder: operator, manual local session; no dependencies added.

## Rulings

1. Phone shell uses Vehicles, Dashboard, Records, Reminders, and Settings.
2. Statistics and Portability remain secondary destinations for later slices.
3. Vehicle distance, volume, energy, and currency configuration is edited in
   vehicle context and displayed from the active vehicle.

## Implementation

- Replaced the single-text activity with a Compose app shell and five-item
  bottom navigation.
- Added repository-backed `AppViewModel` and shell state.
- Added Vehicles screen with empty state, active vehicle cards, archived
  section, vehicle switching, and vehicle creation form.
- Added vehicle-scoped distance/volume/energy/currency selectors.
- Added Dashboard shell with current reading, active vehicle context, quick
  actions, and placeholder sections reserved for Slices 08–09.
- Added Settings shell with active vehicle configuration shortcut.
- Added teal-led light/dark Material theme tokens.
- Added shell tests for empty state, independent vehicle configurations, and
  accessible configuration labels.

## Gate

```text
./gradlew --offline --no-daemon :core:test :app:testDebugUnitTest
BUILD SUCCESSFUL in 45s
EXIT=0
```

Full feature screens for records, reminders, statistics, and portability are
intentionally deferred to their assigned slices.
