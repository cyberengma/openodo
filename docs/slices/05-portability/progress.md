# Progress — 05-portability

## Rulings

1. Both required fixtures are present. `drivvo-sample.csv` is a fresh
   anonymized export; `fuelio-sample.csv` is an older legacy export and will
   be treated as a compatibility fixture, not as proof of the current Fuelio
   format. The `Enigma` driver value is intentionally anonymized.
2. Drivvo income and route rows are skipped. Income uses
   `SKIP/UNSUPPORTED_INCOME`; route rows use `SKIP/UNSUPPORTED_ROUTE`.
3. Drivvo rows set `missedPreviousFillUp = false`; the first imported row
   receives an informational note documenting that Drivvo has no equivalent
   missed-fill flag.

## Session plan

Session 1: BackupV1, RFC-4180 reader/writer, CSV round-trips, and tests.
Session 2: CSV export and fixture importers, reports, and tests.
Session 3 if needed: format documentation, golden file, and report polish.

Implementation has started locally; no dependencies are added.

2026-09-13, local session complete:
- BackupV1, RFC-4180 CSV, CSV export, and both fixture importers implemented.
- 13 portability tests and 173 total core tests pass.
- Final offline gate passes.
- Drivvo fixture: 183 fuel entries imported; report counts are
  `INFO/DRIVVO_MISSED_DEFAULT_FALSE=1`, `SKIP/INVALID_DATE=2`, and
  `SKIP/INVALID_VOLUME=75`.
- Fuelio legacy fixture: 2 fuel entries imported with no report notes.
- The supplied fixture files are intentionally anonymized and committed as
  protected real-world compatibility inputs.
