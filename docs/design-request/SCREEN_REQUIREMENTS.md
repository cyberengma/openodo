# Screen Requirements

Each screen specification must include: purpose, entry points, layout
hierarchy, fields/content, primary action, secondary actions, navigation
result, loading state, empty state, warning state, error state, delete or
cancel behavior, accessibility semantics, and small-screen behavior.

## 1. Vehicle selection and vehicle empty state

Support:

- First launch with no vehicles.
- Add-vehicle action.
- List of active vehicles.
- Archived vehicle access.
- Active vehicle indication.
- Vehicle switching.
- Vehicle archive confirmation.
- Empty, loading, and local-storage error states.

Define the vehicle card anatomy: name, make/model, year, optional photo,
current odometer, and archived state. Long names must not break the layout.

## 2. Vehicle creation and editing

Specify fields for name, make, model, year, VIN, photo, distance unit,
volume unit, energy unit, currency, manual odometer, and notes.

Distance, volume, energy, and currency choices belong to this vehicle and
are persisted with it. Switching vehicles must switch all display formatting
to the selected vehicle's configuration. One vehicle may use kilometres,
litres, and CAD while another uses miles, gallons, and USD.

Define required versus optional fields, invalid input behavior, save/cancel
behavior, unsaved-change confirmation, and archive behavior. The design must
not imply VIN or plate data is transmitted anywhere.

## 3. Dashboard

Show, in priority order:

1. Active vehicle context and vehicle switcher.
2. Overdue and due-soon reminders.
3. Primary actions: add fuel, add expense, add reminder.
4. Current odometer and stale-odometer indication.
5. Fuel economy and cost summary.
6. Recent fuel entry and recent expense.
7. Secondary trends or navigation to statistics.

Define separate empty states for no fuel, no expenses, and no reminders.
Offline must not be presented as an error.

## 4. Fuel entry

Support both liquid and electric entry modes.

Liquid fields:

- Date
- Odometer
- Volume
- Unit price
- Total cost
- Fuel label
- Full tank
- Missed previous fill-up
- Station name
- Receipt photo
- Notes

Electric fields:

- Date
- Odometer
- Energy
- Total cost
- Fuel/charging label
- Full charge indicator where applicable
- Station name
- Receipt photo
- Notes

Define conditional fields, unit/currency display, date picker, numeric input,
save/edit/cancel behavior, receipt permission/failure states, and validation
for missing/both/non-positive measurements, negative cost, future date, and
odometer-decrease warning.

## 5. Fuel history and entry detail

Support chronological history, liquid/electric filtering, fuel-label
filtering, date ranges, full/partial indicators, missed-fill indicators,
economy values, cost, edit, delete, and detail display.

Define filter sheet behavior, sort behavior, delete confirmation, import
origin indicators, and incomplete imported-row presentation.

## 6. Expense record creation and editing

Fields:

- Category: Service, Repair, Upgrade, Other.
- Record type, including custom types.
- Date and odometer.
- Title and description.
- Cost.
- Performed by: Self or Shop.
- Shop name.
- Warranty-until date.
- Receipt photo.
- Notes.

Define category/type selection, conditional shop behavior, validation,
matching-reminder reset feedback, edit/delete, receipt handling, and
unsaved-change behavior.

## 7. Expense and records list/detail

Design the unified record list with category/type, date, odometer, cost,
performed-by, shop, warranty, title, and notes indicators.

Support filtering by category, type, date range, and shop; text search;
empty state; detail view; edit; and delete confirmation.

## 8. Reminders list

Display reminder type, intervals, next due date, next due odometer,
remaining days/distance, trigger axis, active state, stale odometer, and
one of these states:

- OK
- Due soon
- Overdue
- Inactive
- Unanchored

Define status badges that work without color alone. Sort overdue first, then
due-soon, then OK, with deterministic ties. Unanchored and inactive states
must be understandable rather than appearing broken.

## 9. Reminder creation, editing, and detail

Fields:

- Reminder type.
- Distance interval.
- Month interval.
- Active state.
- Anchor date.
- Anchor odometer.

Both intervals use whichever comes first. At least one positive interval is
required. Define manual reset, edit, deactivate, delete, no-anchor behavior,
and matching-expense auto-reset feedback.

Detail must show status, due values, remaining values, trigger reason, stale
odometer warning, current anchor, and reset actions.

## 10. Statistics and trends

Design views for:

- L/100 km
- km/L
- US MPG
- UK MPG
- kWh/100 km
- miles/kWh
- Cost per distance
- Best/worst valid span
- Monthly cost
- Monthly volume
- Monthly energy
- Unit price by fuel label

Support date ranges and liquid/electric separation. Invalid or missed-fill
spans must be excluded and explainable. Every chart needs a textual summary,
explicit units, accessible labels, and a no-data/insufficient-data state.

## 11. Settings

Include vehicle management, active vehicle, fuel labels, default record
types, reminder preferences, backup/export, import, receipt/photo handling,
about/license, and privacy/offline information.

Do not place distance unit, volume unit, energy unit, or currency in global
settings. Those values are edited from each vehicle's create/edit screen.
Settings may provide a shortcut to edit the active vehicle's configuration,
but must make the vehicle scope explicit.

Do not include accounts, sync, analytics, network settings, or online-only
features.

## 12. Backup, import, and export flows

Define entry points and flows for JSON backup restore, JSON backup export,
CSV export, Drivvo import, and Fuelio import.

Import confirmation must show imported, warning, and skipped counts plus
report codes. Define malformed CSV, unsupported schema, cancellation,
duplicate application, and local file access failure states. No file is
uploaded or transmitted.
