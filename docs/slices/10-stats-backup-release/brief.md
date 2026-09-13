# Slice 10 — stats-backup-release

Builds on Slices 08–09, the approved design authority, and the F-Droid
release checklist.

## Goal

Complete statistics, backup/restore/import UI, release metadata, reproducible
release CI, and F-Droid submission artifacts.

## Scope: In

1. Statistics screens with date range, liquid/electric separation, all
   economy conversions, costs, monthly buckets, valid-span explanations,
   textual chart alternatives, and empty/insufficient-data states.
2. SAF JSON backup export/restore and CSV export UI. Preserve every vehicle's
   units and currency configuration.
3. Drivvo/Fuelio import UI with confirmation, imported/warning/skipped report
   counts, row detail, malformed-file errors, unsupported schema errors, and
   cancellation handling.
4. Fastlane metadata, asset license inventory, changelog, and screenshots.
5. Reproducible release workflow: clean tag checkout, unsigned release
   build, deterministic proof, and signing with build-tools 34.0.0.
6. Draft F-Droid metadata, lint/build documentation, and release handoff.

## Scope: Out

- New domain features, cloud sync, accounts, analytics, or post-MVP formats.

## Acceptance

- Two clean unsigned release builds are byte-identical with proof recorded.
- `assembleRelease` works from a clean tag checkout.
- F-Droid metadata and fastlane requirements are complete.
- Backup/import UI matches the existing canonical portability schema.
- Offline gate and release checks pass.
