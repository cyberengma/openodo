# RC-6 — Release Candidate Verification

## Goal

Verify the final candidate as an installable product before publishing.

## Scope

- Build debug, unsigned release, and signed release artifacts where the
  operator has supplied the signing key through the approved path.
- Fresh-install test on a clean emulator/device.
- Upgrade test from the previous debug/candidate build.
- Vehicle-scoped units/currency test with at least two vehicles.
- Fuel, expense, reminder reset, deletion/re-anchor, statistics, backup,
  restore, Drivvo import, Fuelio import, and receipt tests.
- Offline launch and offline operation test.
- APK metadata, version, package, SDK, and signature verification.
- Reproducibility proof review.
- Final release notes and known-limitations review.
- Tag and publish only after explicit operator approval.

## Acceptance

- Candidate installs, launches, survives restart, and supports the core
  end-to-end workflow without network access.
- Backup/restore and real-fixture import are verified on-device.
- Release artifacts and checksums are archived in the handoff.
- No known blocker remains before the tag.
