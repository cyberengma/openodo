# Fuel Entry

## Modes

The form starts with a clear Liquid Fuel/Electric Charging segmented control.

Liquid fields: date, odometer, volume, unit price, total cost, fuel label,
full tank, missed previous fill-up, station, receipt, notes.

Electric fields: date, odometer, energy, total cost, charging label, full
charge, station, receipt, notes.

## Behavior

Use the active vehicle's units and currency in every label. Show calculated
interval distance and cost previews. Missing/both measurements, non-positive
measurements, negative cost, and future dates block save. Odometer decrease is
a warning and does not silently disappear.

Save, cancel, edit, receipt attachment, permission failure, and unsaved-change
confirmation must be explicit. Electric charging is a first-class mode, not
an obscure advanced option.
