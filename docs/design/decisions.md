# Design Decisions

## D1: vehicle-scoped display configuration

Distance, volume, energy, and currency belong to `Vehicle`. Global Settings
does not override them. This supports multiple vehicles with different
regional conventions and preserves canonical storage.

## D2: five-item mobile shell

Primary destinations are Vehicles, Dashboard, Records, Reminders, and
Settings. Statistics is reached from dashboard/history metrics; Portability
is reached from Settings.

## D3: reminder urgency

Overdue, due soon, OK, unanchored, and inactive use text/icon/order as well
as restrained color. Both intervals use whichever comes first.

## D4: offline language

The app states privacy/offline guarantees but never presents offline as a
degraded connection state or offers cloud sync.

## Open decisions

None blocking for authoring Slices 06-10. Slice briefs may refine exact copy,
chart library treatment, and persistence error wording without changing the
decisions above.
