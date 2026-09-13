# RC-3 Progress

Completed in this increment:

- Repository-backed fuel and expense delete actions in record cards.
- Local receipt URI capture field in the fuel flow.
- Transactional JSON restore/export and CSV actions remain available from
  the Data surface.
- Release build continues to pass offline.

Remaining before RC-3 can be closed:

- Full edit flows rather than delete-only record cards.
- Android document-picker URI capture replacing the current local receipt
  marker in all forms.
- Complete statistics visualization/data wiring.
- Screenshot/accessibility review against the Sleek export.

2026-09-13 additions:
- Vehicle edit form now includes distance, volume, energy, and currency
  selectors plus make/model.
- Receipt "View" action on fuel and expense cards (local ACTION_VIEW, no
  upload).
- Import preview now shows per-row report detail (row + code).

Reproducibility proof:
- Two clean `assembleRelease` runs produce byte-identical APKs.
- SHA-256: 48583d5f156cf54efc7e3ae5e8c57889acb50c40872c65fe6fec3f2aec66d222
