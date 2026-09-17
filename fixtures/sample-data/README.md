# Sample data

Generic, fictional seed data (no personal information) for screenshots and
manual testing.

- `openodo-sample-backup.json` — a `BackupV1` backup containing one vehicle
  ("Civic Touring"), 40 record types, 12 fuel entries (liquid + one electric),
  8 expense records across Service/Repair/Upgrade/Other, and 4 reminders.

## How to load it

1. Copy `openodo-sample-backup.json` to your device.
2. In OpenOdo, open **Data** → **Restore JSON backup** and pick the file.

Restore atomically replaces the local database, so use a fresh install or a
scratch profile if you want to keep other data.