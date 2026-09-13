# State Matrix

Every data screen uses these states:

| State | Required behavior |
|---|---|
| Loading | Skeleton preserves final hierarchy; no misleading zero values. |
| Loaded | Show data and active vehicle configuration. |
| Empty | Explain why it is empty and give the first useful action. |
| Validation error | Block save, associate message with field, preserve input. |
| Warning | Non-blocking; label cause and consequence. |
| Local error | Explain local failure and provide retry; no network suggestion. |
| Cancelled | Return without mutation or loss of existing data. |
| Destructive action | Confirm; state impact and reversibility. |

Required domain-specific variants include stale odometer, odometer decrease,
missed fill, invalid statistics spans, unsupported backup schema, malformed
CSV, import report, and unsaved form exit.

## Required rendered variants before UI implementation

Sleek's first pass is primarily a populated happy path. Before final UI
implementation, planning must account for these variants:

- Vehicles: no vehicles, one vehicle, multiple vehicles with different
  units/currencies, archived vehicle, local load failure.
- Dashboard: no history, no reminders, stale odometer, local load failure.
- Fuel/expense forms: empty form, invalid fields, odometer warning,
  receipt permission failure, unsaved exit.
- Records/reminders: empty list, filtered-empty list, inactive/unanchored
  reminder, delete confirmation.
- Statistics: no data, insufficient valid spans, mixed-currency error,
  invalid/missed spans excluded.
- Portability: picker cancellation, malformed CSV, unsupported schema,
  import report with imported/warning/skipped counts, local file failure.

These states may be represented as written variants in the design authority
when a rendered Sleek screen does not exist, but implementation must not
invent different behavior.
