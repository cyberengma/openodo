# F-Droid submission checklist — OpenOdo

Source: f-droid.org Inclusion Policy, Submitting Quick Start Guide, and
Reproducible Builds docs (checked 2026-09-12). Items marked (worker) land
in slice 10; items marked (operator) are yours.

## Already satisfied by the architecture

- FLOSS license (GPL-3.0-only), SPDX headers, LICENSE at root.
- All dependencies FLOSS and served from Maven Central / Google Maven;
  no Firebase, GMS, Crashlytics, ads, trackers → no anti-features.
- No `INTERNET` permission; nothing downloaded at runtime.
- Unique `applicationId` under a domain you own (`ca.terradevop.openodo`);
  not a fork, so no rebranding rule applies.
- Builds from the command line with a FLOSS toolchain (Debian OpenJDK 21,
  Gradle wrapper, Android SDK — SDK prebuilts are explicitly permitted).
- `versionCode`/`versionName` literal in `defaultConfig`, so
  `AutoUpdateMode: Version` and `UpdateCheckMode: Tags` work with no
  `UpdateCheckData`.
- No secrets, keystores, or signing config in the repo.

## Slice 10 deliverables (worker)

- [ ] `fastlane/metadata/android/en-US/`: `short_description.txt` (≤ 80
      chars, no trailing dot), `full_description.txt` (only shipped
      features — F-Droid rejects descriptions that overpromise),
      `title.txt`, `images/icon.png` (512×512), `images/phoneScreenshots/1..n.png`,
      `changelogs/<versionCode>.txt` (≤ 500 chars).
- [ ] `docs/release/ASSETS.md` listing every non-code asset with its
      license (icon and screenshots included).
- [ ] GitHub Actions release workflow: checkout the tag, `./gradlew
      --no-daemon assembleRelease`, sign with `apksigner` from
      **build-tools 34.0.0** (35+ is unverifiable by apksigcopier), attach
      the APK; keystore and passwords come from repository secrets only.
- [ ] Two consecutive clean release builds produce byte-identical
      unsigned APKs (`diffoscope` or `sha256sum` in the handoff) — the
      red proof for reproducibility.
- [ ] `docs/release/fdroiddata.yml`: a draft `metadata/ca.terradevop.openodo.yml`
      with `Categories: [Money]` (Autu Mandu's category; confirm at submission),
      `License: GPL-3.0-only`, `SourceCode`, `IssueTracker`, `Changelog`,
      `RepoType: git`, `Repo`, one `Builds:` block (`subdir: app`,
      `gradle: [yes]`), `AutoUpdateMode: Version`, `UpdateCheckMode: Tags`,
      `CurrentVersion`/`CurrentVersionCode`, plus `AllowedAPKSigningKeys`
      and `Binaries:` pointing at the GitHub release asset URL pattern.
- [ ] `fdroid lint` and `fdroid build` pass in the fdroidserver container
      against the draft (documented as a repeatable script under
      `docs/release/`).

## Operator tasks (you)

- [ ] Mirror the private bot repo to a **public** repo before v1.0; the
      public repo is what `Repo:` points at and must hold real source, not
      placeholders.
- [ ] Generate the release keystore once, store it only in CI secrets and
      an offline backup. Reproducible-build publishing uses **your**
      signature forever; the key cannot change later without users
      reinstalling.
- [ ] Tag `v1.0.0` on the release commit; build and publish the signed
      APK from CI at that tag (never from Android Studio or a dirty tree).
- [ ] Fork `fdroiddata`, add the metadata file, run the container
      `fdroid readmeta && fdroid lint && fdroid build ca.terradevop.openodo`,
      push, open the merge request labelled `New App`.
- [ ] Reply to packager questions promptly; expect 24–48 h after merge
      before the app appears.
- [ ] Positioning for the "unique value" review: Autu Mandu (Car Report
      successor) already exists on F-Droid. OpenOdo is an original
      codebase; the description should lead with what it does that Autu
      Mandu doesn't — interval reminders with auto-reset, unified
      service/repair/upgrade/other records, Drivvo/Fuelio importers,
      Material You.
