# Product Context

## Product

OpenOdo is a free, GPLv3-only, privacy-first Android application for
recording a vehicle's life offline:

- Fuel fill-ups and electric charging.
- Service, repair, upgrade, and other expenses.
- Maintenance reminders that automatically reset when matching work is logged.
- Fuel economy, cost, and trend statistics.
- Drivvo and Fuelio CSV import.
- JSON backup and CSV export.
- Receipt photo filenames and local receipt-photo handling.

There are no ads, trackers, analytics, crash SDKs, Google Play Services,
cloud accounts, or `INTERNET` permission.

## Users

The primary user is a vehicle owner who wants a durable, private history of
fuel, maintenance, expenses, and upcoming work. The user may have one or
several vehicles, may enter data sporadically, and may import an old history.
The app must remain useful when the dataset is initially empty.

## Existing technical context

- Package/application id: `ca.cyberengma.openodo`.
- Android UI: Jetpack Compose and Material 3.
- Domain logic: pure Kotlin in `core`; UI and platform I/O in `app`.
- Persistence is planned for Slice 06.
- UI work begins with Slice 07 after the design authority is approved.
- The design package must support Slices 07–10.

## Canonical values

The domain stores exact values, not display values:

| Concept | Canonical storage |
|---|---|
| Distance | metres as `Long` |
| Volume | millilitres as `Long` |
| Energy | watt-hours as `Long` |
| Money | integer minor currency units as `Long` |
| Unit price | milli-units of major currency as `Long` |
| Record date | ISO `LocalDate` |
| Audit timestamps | epoch milliseconds |

Each vehicle owns its display configuration: distance unit, volume unit,
energy unit, and currency. Switching vehicles must switch all unit and
currency formatting with the active vehicle. The UI must label every
displayed number with the active vehicle's unit and currency. Global settings
must never change the interpretation of another vehicle's stored data.

## Domain capabilities already defined

### Fuel

Fuel entries are either liquid or electric. Liquid entries use volume;
electric entries use energy. Entries can be full-tank/full-charge or partial.
Missed fill-ups invalidate the affected consumption span. Invalid spans are
excluded from valid statistics.

### Expenses

One expense record table covers four categories:

- Service
- Repair
- Upgrade
- Other

Each record can use a default or custom record type and may contain shop,
warranty, receipt, description, and notes.

### Reminders

Reminder states are `OK`, `DUE_SOON`, `OVERDUE`, `INACTIVE`, and
`UNANCHORED`. Date and distance intervals are supported; both intervals use
“whichever elapses first.” A matching expense automatically resets the
reminder anchor. A stale odometer is flagged separately without creating a
new reminder state.

### Portability

Backup is versioned JSON. CSV import produces row-level INFO, WARN, and SKIP
reports. Import must never silently discard invalid rows.

## Product tone

The interface should feel dependable, calm, practical, and data-respectful.
Avoid gamification, excessive decoration, dashboard noise, or imagery that
implies cloud connectivity. Urgency should be clear without being alarming.
