# Slice 05 — portability

Written against architecture v0.1. The architecture wins on conflict; gaps
become questions in progress.md.

Builds on slices 02–04 per their acceptance items: complete `core.model`,
`core.fuel` spans (used to validate imports), `core.reminders`
(reminders are part of the backup).

## Goal

The user can leave with everything and arrive with everything: a
versioned JSON backup format that round-trips the full domain losslessly,
CSV export for spreadsheets, and importers that read real Drivvo and
Fuelio CSV exports into `core.model` objects with a report of what was
mapped, converted, and skipped. All of it in `core`, tested against
checked-in fixture files.

## Scope: In

1. `core.portability.backup.BackupV1` — kotlinx-serialization schema with
   `schemaVersion = 1`, `exportedAt`, `appVersion`, and arrays of
   vehicles, record types, fuel entries, expense records, reminders,
   plus a `photos` manifest (file names only — the zip layer is `app`'s
   job in slice 10). `write(domain): String` and `read(json):
   Result<Domain, BackupError>`; unknown fields ignored, missing
   optional fields defaulted, wrong `schemaVersion` → typed error.
2. Canonical values only in the JSON (metres, mL, Wh, minor units,
   ISO dates) — never display units.
3. `core.portability.csv.Rfc4180` — a dependency-free reader/writer
   (quotes, embedded commas/newlines/quotes, CRLF and LF, optional BOM,
   header row). Streaming reader over a `Reader`; no whole-file `String`
   requirement.
4. `core.portability.csv.CsvExport` — one CSV per entity type with a
   documented header (in `docs/formats/csv.md`, written in this slice),
   display units chosen by the caller, ISO dates, `.` decimal separator.
5. `core.portability.import.DrivvoImporter` and `FuelioImporter` —
   detect format from the header, map columns to `FuelEntry` /
   `ExpenseRecord` / `RecordType` (creating custom types where no seed
   type matches), convert units per the file's declared units into
   canonical, and return `ImportResult(entities, report:
   List<ImportNote(row, level ∈ {INFO, WARN, SKIP}, code, message)>)`.
   Rows with unparseable dates or odometers are `SKIP`, never fatal.
6. Fixtures in `fixtures/imports/` (protected): the operator supplies
   `drivvo-sample.csv` and `fuelio-sample.csv` — anonymised real exports
   — before this slice ignites. Tests assert exact row counts, the first
   and last entry's converted values, and the full set of report codes
   for each fixture.
7. Round-trip tests: domain → BackupV1 → domain equality; domain →
   CSV → parsed rows equality on every column; a backup with an unknown
   future field still reads; `schemaVersion = 2` rejected with the typed
   error.

## Scope: Out

- File/zip/SAF I/O, share sheets, progress UI (slice 10).
- Merging an import into an existing dataset (v1 imports into a new
  vehicle only; dedupe is post-MVP).
- Other apps' formats (aCar, Simply Auto) — post-MVP.

## Open questions (propose-first)

1. **If either fixture file is absent at session start, stop and write
   the question** — do not fabricate a sample from memory of the
   format. The operator will add the file and resume.
2. Drivvo exports include income and "route" rows; propose whether
   income maps to `OTHER` with negative cost (recommendation: skip with
   `SKIP/UNSUPPORTED_INCOME`), and routes are skipped.
3. Fuelio's "missed fill-up" and "partial" columns map directly; Drivvo
   only has "full tank". Propose how Drivvo rows should set
   `missedPreviousFillUp` (recommendation: always false, with an INFO
   note on the first row).

## Acceptance

- Gate command exits 0 from a clean clone with the fixture files
  present.
- `docs/formats/csv.md` and `docs/formats/backup-v1.md` exist and match
  the code (the test for the backup schema serializes a known domain
  and compares against `core/src/test/resources/backup/v1-golden.json`,
  which this slice creates and commits; the handoff quotes its first 20
  lines).
- Red proof: the RFC-4180 reader shown failing on a deliberately
  corrupted quoted-newline case before passing; the `schemaVersion`
  guard shown failing when the check is commented out.
- Handoff shows the import report for each fixture in full (counts per
  code).

## Session plan

- Session 1: items 1–3, 7 (backup + CSV core, round-trips).
- Session 2: items 4–6, importers against fixtures; open questions 2–3
  written before mapping ambiguous columns.
- Session 3 (if needed): report polish, formats docs, golden file.
