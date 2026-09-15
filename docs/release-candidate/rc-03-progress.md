# RC-3 Progress

Completed in this increment:

- Repository-backed fuel and expense delete actions in record cards.
- Local receipt URI capture field in the fuel flow.
- Transactional JSON restore/export and CSV actions remain available from
  the Data surface.
- Release build continues to pass offline.

Remaining before RC-3 can be closed:

- On-device screenshot/accessibility review against the Sleek export at large
  text, narrow-screen, landscape, light-theme, and dark-theme configurations.

2026-09-13 additions:
- Vehicle edit form now includes distance, volume, energy, and currency
  selectors plus make/model.
- Receipt "View" action on fuel and expense cards (local ACTION_VIEW, no
  upload).
- Import preview now shows per-row report detail (row + code).

2026-09-15 additions:
- Unified, month-grouped History timeline for fuel and expense records with
  category filters, expandable details, and edit/delete actions.
- Fuel and expense forms now provide complete edit flows, including expense
  date, description, warranty, notes, and receipt fields.
- Fuel and expense receipt selection uses Android's document picker and retains
  read permission; attached receipts remain viewable from History.
- Statistics period and liquid/electric filters now affect calculations and
  include L/100 km, km/L, US/UK MPG, kWh/100 km, mi/kWh, cost per distance,
  best/worst spans, exclusion explanations, monthly cost/volume/energy, and
  unit-price summaries.
- Monthly charts include explicit units and a textual data alternative.
- Static accessibility review found no unlabeled icon-only actions in the new
  surfaces; decorative icons inside labeled controls are hidden from semantics.
- Offline `:core:test`, `:app:testDebugUnitTest`, and `:app:assembleRelease`
  gate passes.

Reproducibility proof:
- Two clean `assembleRelease` runs produce byte-identical APKs.
- SHA-256: 48583d5f156cf54efc7e3ae5e8c57889acb50c40872c65fe6fec3f2aec66d222
