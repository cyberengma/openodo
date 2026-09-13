# Dashboard

## Purpose

Give the active vehicle a useful overview and make logging one tap away.

## Hierarchy

1. Active vehicle switcher.
2. Current odometer and stale-reading metadata.
3. Overdue and due-soon reminders.
4. Add Fuel, Add Expense, Add Reminder quick actions.
5. Fuel economy and cost metrics.
6. Recent activity.

Use cards with restrained borders, not excessive gradients. Metrics always
include the active vehicle's unit/currency.

## States

- No vehicles: route to Vehicles setup.
- No reminders: show a useful add-reminder action.
- No history: show separate fuel and expense prompts.
- Stale odometer: non-blocking warning with last-known date.
- Local read error: retry action and preserve vehicle context.
- Loading: preserve card hierarchy with skeleton values.
