# Navigation

## Primary shell

Use a five-destination bottom navigation on phones:

1. Vehicles
2. Dashboard
3. Records
4. Reminders
5. Settings

Statistics is reached from the Dashboard metric card and Records/Fuel
History summaries. Portability is reached from Settings. The active vehicle
is persisted and shown in the top app bar on vehicle-dependent screens.

## Flows

```text
First launch -> Vehicles empty -> Add vehicle -> Dashboard
Vehicles -> select vehicle -> Dashboard
Dashboard -> Add Fuel -> Fuel form -> save -> Dashboard
Dashboard -> Add Expense -> Expense form -> save -> Records/Dashboard
Dashboard -> Add Reminder -> Reminder form -> save -> Reminders
Records -> record detail -> edit/delete
Reminders -> detail -> edit/manual reset/deactivate/delete
Settings -> Portability -> import/export/backup result
Dashboard/Records -> Statistics -> date range and metric filters
```

## Navigation rules

- Back from a form with changes opens an unsaved-changes confirmation.
- Successful creation returns to the originating list or dashboard.
- Delete always confirms and states whether it can affect reminder anchors.
- Switching vehicles keeps the destination but refreshes all vehicle-scoped
  values and units.
- Empty primary destinations explain the first useful action rather than
  showing a blank surface.
- No destination implies online sync or remote storage.
