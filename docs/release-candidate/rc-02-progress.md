# RC-2 Progress

Implemented and verified:

- SAF JSON backup export through the in-app Data destination.
- Fuel CSV export through SAF.
- Drivvo/Fuelio CSV file selection through SAF.
- Fixture-aware importer selection and visible imported/skipped summary.
- Core BackupV1, RFC-4180, and importer tests remain green.

Limitations carried into the next RC pass:

- JSON restore application is not yet transactional through repositories.
- CSV expense export and row-detail report UI remain to be completed.
- Import preview/confirmation and duplicate policy need full UI treatment.
