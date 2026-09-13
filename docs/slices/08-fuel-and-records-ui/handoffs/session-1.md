# Handoff — 08-fuel-and-records-ui, session 1

Builder: operator, manual local session; no dependencies added.

## Rulings

1. Records uses a unified destination with Fuel and Expenses modes.
2. Forms use the active vehicle's display units and currency while writing
   canonical core values through repository models.
3. Full feature detail, receipts, and reminder-reset feedback remain explicit
   follow-up refinements within this slice before production UI completion.

## Implemented

- Added repository-backed fuel and expense flows to the Records destination.
- Added liquid/electric mode selector and active vehicle unit labels.
- Added fuel fields for odometer, volume/energy, cost, label, and full state.
- Added validation for positive measurements and non-negative costs.
- Added expense category selector for Service, Repair, Upgrade, and Other.
- Added title/cost validation and canonical `Money` creation.
- Added empty states and timeline cards for fuel and expense records.
- Kept calculations and canonical conversions in core/domain types.

## Gate

```text
./gradlew --offline --no-daemon :core:test :app:testDebugUnitTest
BUILD SUCCESSFUL in 51s
EXIT=0
```
