# OpenOdo — Architecture

**v0.1**

This document wins on conflict during sessions; changes happen only in
planning. Gaps become questions in the active slice's `progress.md`.

## What this is

OpenOdo is a free, GPLv3, privacy-first Android app for logging a vehicle's
life: fuel fill-ups, service/repair/upgrade/other expenses, and interval
reminders that auto-reset when the matching work is logged. Everything is
offline, stored locally, exportable, and importable from Drivvo and Fuelio
CSV exports. No ads, no trackers, no `INTERNET` permission.

Working name and `applicationId` (`ca.terradevop.openodo`) are placeholders
until release; renaming pre-release is a planning change, not a worker task.

## Stack (pinned in `gradle/libs.versions.toml` by slice 01)

| Layer | Choice |
|---|---|
| JDK | 21 — Debian Trixie `openjdk-21` (what F-Droid's buildserver ships); bytecode target 17 |
| Build | Gradle via wrapper, AGP 8.x, Kotlin 2.x (K2), KSP; repositories `google()` + `mavenCentral()` only |
| SDK | compileSdk 36, targetSdk 36, minSdk 26 (java.time native — no desugaring) |
| `core` module | Kotlin/JVM, zero Android imports, zero DI, deps: kotlin-stdlib, kotlinx-serialization-json, junit4 + kotlin-test |
| `app` module | Jetpack Compose (BOM) + Material 3, Hilt (KSP), Room (KSP, schema export to `app/schemas/`), WorkManager, Coil, Vico charts, Robolectric + Turbine for tests |
| Test runner | JUnit 4 in both modules |

`core` owns every business rule. `app` owns persistence, UI, notifications
and platform I/O, and calls `core`. If a rule needs Android, it is in the
wrong module.

## Gate contract

The gate is `clone && ./gradlew --offline --no-daemon :core:test
:app:testDebugUnitTest` inside the image built from `gate/Dockerfile`, with
no network. Therefore:

1. Every dependency and plugin is declared in `gradle/libs.versions.toml`
   (never inline). Gradle dependency locking is on for all configurations
   in every module and lockfiles are committed. `libs.versions.toml` is the
   contract's lockfile key.
2. Changing the dependency set ends the session on the designed gate
   refusal; the operator rebuilds the image. Front-load dependencies in
   slice 01; later slices may not add any without an open question first.
3. Tests never touch network, filesystem outside the build dir, real
   time, or locale defaults. `Clock`, `ZoneId`, `Locale` are injected.
4. Robolectric (slices 06+) must run with `robolectric.offline=true`
   against jars warmed into the gate image — a planning task before 06.

## Conventions workers follow

- **Canonical units in storage and in `core`**: distance in metres
  (`Long`), volume in millilitres (`Long`), energy in watt-hours (`Long`),
  money in minor units (`Long`) with an ISO-4217 code, unit price in
  milli-units of the major currency (`Long`, 1.599 → 1599). Display units
  (km/mi, L/gal, kWh) are view-layer conversions only. No `Double` in a
  stored or serialized field.
- **Dates**: record dates are `LocalDate`; audit stamps (`createdAt`,
  `updatedAt`) are epoch millis. Never convert a record date through a
  zone.
- **Records model**: one `ExpenseRecord` with `category ∈ {SERVICE, REPAIR,
  UPGRADE, OTHER}` and a `typeId` into `RecordType(category, name,
  isDefault)`. Separate screens, one table. Fuel is its own entity
  (`FuelEntry`), not an expense record.
- **Fuel consumption**: computed over spans between consecutive
  `fullTank = true` entries; partial fills inside a span add their volume
  to the span; a span containing an entry with `missedPreviousFillUp =
  true` is excluded; spans need distance > 0 and volume > 0. Output is
  canonical (mL per 100 km); MPG/km/L are display conversions.
- **Reminders**: one active reminder per (vehicle, typeId). Anchor =
  `(anchorDate, anchorOdometerMetres)`; due when either interval elapses;
  status `OK / DUE_SOON / OVERDUE`; `DUE_SOON` = within 10% of the interval
  or 30 days / 500 km, whichever is smaller for that axis. Logging an
  expense record with the reminder's `typeId` resets the anchor to that
  record's date and odometer.
- **Current odometer** = max over all records for the vehicle, unless a
  manual override is newer than every record.
- **Errors**: `core` returns sealed `Result`-style types; it never throws
  across the module boundary for expected failures.
- **Serialization**: kotlinx-serialization; the backup JSON schema is
  versioned (`schemaVersion`) and owned by `core.portability`.
- **Package layout**: `core`: `model`, `units`, `money`, `validation`,
  `fuel`, `reminders`, `stats`, `portability`. `app`: `data` (Room,
  repositories), `ui/<feature>`, `ui/theme`, `ui/navigation`, `work`.
- **Fixtures** live in `fixtures/` (protected). Tests read them by
  relative path from the repo root; a test needing a variant builds it in
  memory.
- **Handoffs** go to `docs/slices/NN-<name>/handoffs/session-S.md`.
- **License**: GPL-3.0-only. `LICENSE` at root; SPDX header
  `// SPDX-License-Identifier: GPL-3.0-only` on every source file. Every
  asset (icon, artwork) carries a FLOSS or CC-BY-SA license note in
  `docs/release/ASSETS.md`; nothing copied from other apps.

## F-Droid compliance (release-shaped rules, enforced from slice 01)

- `versionCode`/`versionName` are literal values in `app/build.gradle.kts`
  `defaultConfig`, never computed; each release is a git tag `vX.Y.Z`
  equal to `versionName` — F-Droid's checkupdates reads both by regex.
- No `INTERNET` permission, no analytics/crash SDK, no Google Play
  Services, no JitPack; nothing downloaded at runtime. Zero anti-features.
- No signing config, keystore, or credentials in the repo. Release APKs
  are built in CI from a clean checkout of the tag with
  `./gradlew assembleRelease`, then signed with `apksigner` from
  **build-tools 34.0.0** (35+ produces APKs F-Droid cannot verify).
- Reproducible-build hygiene: `tasks.whenTaskAdded { if
  (name.contains("ArtProfile")) enabled = false }`, no resource shrinker,
  `vcsInfo` left on, R8 keep rules for coroutines `ServiceLoader`
  entries, PNGs pre-optimised and `cruncherEnabled = false`.
- The repo ships `fastlane/metadata/android/en-US/` (short_description
  ≤ 80 chars, full_description, `images/icon.png`,
  `images/phoneScreenshots/1.png…`, `changelogs/<versionCode>.txt` ≤ 500
  chars). `full_description` lists only shipped features.
- The source repo must be **public** at submission; the factory's
  private bot repo is mirrored to a public one at v1.0 (operator task).

## Slice plan

| # | Slice | Deliverable (gate-provable) | Depends on |
|---|---|---|---|
| 01 | scaffold | Two-module Gradle project, all deps declared + locked, `gate/Dockerfile` builds, one real `core` test, LICENSE | — |
| 02 | domain-units-money | `core.model`, `units`, `money`, `validation` with tests | 01 |
| 03 | fuel-engine | Consumption spans, per-entry economy, trend series, edge cases | 02 |
| 04 | reminders-engine | Due calc, status, auto-reset, current-odometer rule | 02 |
| 05 | portability | Backup JSON v1, CSV export, Drivvo + Fuelio importers vs fixtures | 02, 03, 04 |
| 06 | persistence | Room schema v1 + repositories mapping to `core` (Robolectric) | 05 |
| 07 | app-shell | Theme, navigation, vehicle CRUD, dashboard | 06, `docs/design/` |
| 08 | fuel-and-records-ui | Entry forms, lists, filters, receipt photos | 07 |
| 09 | reminders-ui | Reminders tab, WorkManager daily check, notifications | 07, 04 |
| 10 | stats-backup-release | Charts, SAF backup/restore, import UI, fastlane metadata, reproducible release CI, `fdroiddata` YAML draft | 08, 09 |

Briefs exist for 01–05. Briefs for 06–10 are authored in planning once
`docs/design/` holds the screen specs; the chain stops honestly after 05
until then.

## ADR log

(empty)
