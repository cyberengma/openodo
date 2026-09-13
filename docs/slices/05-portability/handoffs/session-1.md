# Handoff — 05-portability, session 1 (single local session)

Builder: operator, manual local session; no new dependencies and no remote
server activity.

## Rulings

1. Both fixtures are present. Drivvo is a fresh anonymized export; Fuelio is
   an older legacy export and is retained as compatibility coverage, not as
   proof of the current Fuelio format. `Enigma` is the intended anonymized
   driver value.
2. Drivvo income and route rows are skipped. The importer uses typed report
   codes `SKIP/UNSUPPORTED_INCOME` and `SKIP/UNSUPPORTED_ROUTE` when those
   rows occur.
3. Drivvo has no missed-fill column, so imported rows use
   `missedPreviousFillUp = false`; the first row receives
   `INFO/DRIVVO_MISSED_DEFAULT_FALSE`.

## Implementation

- `BackupV1` writes and reads schema version 1 with canonical numeric fields,
  ISO dates, unknown-field tolerance, optional defaults, and typed failure
  through `Result` for invalid JSON or schema versions.
- `Rfc4180` is a dependency-free streaming reader/writer supporting BOM,
  LF/CRLF, embedded commas, quotes, and newlines. Unterminated quoted
  fields are rejected.
- `CsvExport` provides documented vehicle, fuel-entry, and expense-record
  headers with canonical columns and CRLF output.
- `DrivvoImporter` detects the vehicle/refueling sections, converts dates,
  kilometres, litres, costs, and prices, and emits row-level notes.
- `FuelioImporter` detects the legacy `## Vehicle` / `## Log` format and
  imports its ISO dates, kilometre odometers, litre volumes, full flags,
  missed flags, labels, and prices.
- Format documentation lives in `docs/formats/csv.md` and
  `docs/formats/backup-v1.md`.
- Golden resource: `core/src/test/resources/backup/v1-golden.json`.

## Fixture reports

### Drivvo

Fixture shape: `#Vehicle`, `#Refueling`, `#Expense`, and `#Service` sections;
276 parsed CSV rows; 183 refueling entries imported.

```
INFO/DRIVVO_MISSED_DEFAULT_FALSE = 1
SKIP/INVALID_DATE                = 2
SKIP/INVALID_VOLUME              = 75
```

The fixture contains multiline expense/service descriptions. Those rows are
intentionally not refueling records and are skipped by the current importer
with invalid-volume notes.

### Fuelio

Fixture shape: legacy `## Vehicle` and `## Log` sections; 2 log rows; both
rows imported successfully.

```
report = {}
```

Observed converted values include first odometer `424,000 m`, first volume
`33,040 mL`, and final odometer `5,000 m`.

## Red proofs

The required intentionally incorrect assertions were run before correction:

```
BackupV1Test > schema version two is rejected FAILED
    java.lang.AssertionError

Rfc4180Test > corrupted quoted newline is rejected FAILED
    java.lang.AssertionError: expected:<[]> but was:<[[a, b], [unterminated
value]]
```

The parser was then corrected to reject unterminated quoted fields and the
schema guard assertion was restored.

## Tests and final gate

Portability tests: 13. Total core tests: 173. All passed.

```
./gradlew --offline --no-daemon :core:test :app:testDebugUnitTest
BUILD SUCCESSFUL in 41s
EXIT=0
```
