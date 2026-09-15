# Correctness Remediation Progress

Rulings:

- JSON restore validates then atomically replaces the local database.
- Permanent vehicle deletion performs an explicit transactional cascade after
  strong confirmation.
- Navigation will move to a saved real back stack in the next phase.
- Decimal form input is locale-aware and maps exactly to canonical values.

Completed:

- Central vehicle-aware distance, volume, energy, money, and unit-price
  parser/formatter using BigDecimal and CurrencyMinorDigits.
- Regression tests for km/miles/metres, litres/US gal/UK gal/mL, kWh/Wh,
  locale decimal separators, USD/JPY/KWD.
- Fuel, expense, and reminder forms use canonical conversion and core
  validators.
- Expense editing preserves description, warranty, receipt, and notes.
- Record-type initialization waits for repository data.
- Atomic replace restore and import application through Room transactions.
- Transactional expense save/reset and delete/re-anchor.
- Transactional permanent vehicle cascade deletion with UI confirmation.
- Fuel/expense delete confirmations describe consequences.
- Active vehicle fallback after deletion.

Navigation/state phase:

- Replaced the flat destination switch with a saved Compose Navigation
  NavHost for primary tabs, secondary screens, and add forms.
- Empty installs start at Vehicles setup; first vehicle creation replaces the
  setup route with Dashboard.
- Back pops exact route history; Dashboard and empty setup use double-back to
  exit.
- Forms intercept Back/Cancel and confirm before discarding dirty input.
- Bottom navigation is hidden while full-screen forms are open.

Correctness/navigation UI increment:

- Recorded fuel and expense odometers now feed CurrentOdometer and reminder
  evaluation instead of relying only on the manual override.
- Manual reminder reset uses the effective record-derived odometer.
- Statistics now exposes km/L next to L/100 km.
- Vehicle-aware formatting remains centralized in VehicleValueFormatter.
