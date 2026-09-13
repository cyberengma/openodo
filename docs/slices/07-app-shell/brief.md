# Slice 07 — app-shell

Builds on Slice 06 and the approved screen/navigation authority in
`docs/design/`.

## Goal

Create the Compose application shell: vehicle selection, vehicle CRUD,
vehicle-scoped display configuration, navigation, theme, and dashboard.

## Scope: In

1. Implement the five-item phone shell: Vehicles, Dashboard, Records,
   Reminders, Settings. Statistics and Portability are reachable from the
   defined secondary entry points.
2. Implement first-launch empty state, vehicle creation/editing, archive,
   active vehicle switching, and archived vehicle access.
3. Implement per-vehicle distance, volume, energy, and currency selectors.
   Switching vehicles changes formatting everywhere without changing stored
   canonical values.
4. Implement the populated dashboard, empty states, stale odometer warning,
   reminder summary, recent activity, and quick actions.
5. Implement the approved visual tokens, light/dark themes, semantic status
   colors, typography, spacing, and accessibility semantics.
6. Add navigation tests and UI tests for first launch, switching vehicles,
   and unsaved form exit.

## Scope: Out

- Fuel/expense/reminder full feature screens; later slices own those.
- Network, accounts, analytics, and cloud behavior.

## Acceptance

- Every dashboard value uses the active vehicle configuration.
- Two vehicles with different units/currencies can be switched without
  cross-formatting.
- Empty/loading/error/warning states match `docs/design/states.md`.
- UI tests cover navigation and accessibility labels.
- Offline gate passes.
