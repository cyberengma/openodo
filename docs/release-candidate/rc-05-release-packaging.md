# RC-5 — Release Packaging

## Goal

Produce release-shaped metadata and automation without committing secrets or
the production signing key.

## Scope

- `fastlane/metadata/android/en-US/` title, short description, full
  description, icon, screenshots, and changelog.
- `docs/release/ASSETS.md` with license for every non-code asset.
- GitHub Actions release workflow that checks out a tag and runs
  `./gradlew --no-daemon assembleRelease`.
- Signing step uses `apksigner` from build-tools 34.0.0 and repository
  secrets only.
- Two clean unsigned release builds and SHA-256/diffoscope proof.
- `docs/release/fdroiddata.yml` draft for
  `ca.terradevop.openodo`.
- Repeatable fdroidserver lint/build instructions.
- Release version/tag preparation; no tag or signing key is created without
  operator approval.

## Operator prerequisites

- Confirm the public GitHub repository is the release source.
- Generate and securely back up the production keystore outside the repo.
- Add signing key/passwords only as CI secrets.
- Approve app icon and screenshot assets/licenses.
- Approve version name/code and release category/description.

## Acceptance

- Clean unsigned release build succeeds.
- Reproducibility proof is recorded.
- No keystore, password, or signing config is committed.
- Metadata passes local format checks and is ready for fdroidserver.
