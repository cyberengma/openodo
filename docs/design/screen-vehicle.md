# Vehicles

## Purpose

Choose the active vehicle, add a vehicle, edit vehicle identity/configuration,
and view archived vehicles.

## Layout

Top app bar: `Vehicles` and add action. Active vehicle cards show name,
make/model/year, current odometer, optional image, archived state, and
configuration chips such as `km - L - kWh - CAD`.

Vehicle edit fields: name, make, model, year, VIN, photo, distance unit,
volume unit, energy unit, currency, manual odometer, and notes. The four
display choices are persisted on this vehicle, never globally.

Include a canonical-storage callout: changing display configuration does not
convert stored values.

## States and actions

- Empty: explain privacy/offline behavior and show `Add vehicle`.
- Loading: skeleton cards.
- Error: local storage error with retry.
- Archive: confirmation; archived vehicles remain recoverable.
- Save: return to the previous vehicle context or dashboard.

Accessibility: cards expose vehicle name and active/configuration status as
one logical item; configuration chips are text, not color-only indicators.
