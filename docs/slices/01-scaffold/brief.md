# Slice 01 — scaffold

Written against architecture v0.1. The architecture wins on conflict; gaps
become questions in progress.md.

## Goal

A two-module Gradle project (`core` Kotlin/JVM, `app` Android) that
compiles, declares and locks every dependency the whole program will use,
carries the GPLv3 license, and passes one real unit test in `core` — so the
operator can bake the offline gate image once and every later slice runs
against it.

## Scope: In

1. Gradle wrapper (latest stable 8.x), `settings.gradle.kts` with modules
   `:core` and `:app`, Kotlin DSL throughout, `repositories` limited to
   `google()` and `mavenCentral()` in both plugin and dependency
   resolution (no JitPack, no custom Maven URLs).
2. `gradle/libs.versions.toml` declaring **all** of: Kotlin, KSP, AGP,
   kotlinx-serialization (plugin + json), Compose BOM + compose-ui,
   material3, ui-tooling(-preview), activity-compose, navigation-compose,
   lifecycle-viewmodel-compose, Hilt (+ hilt-navigation-compose), Room
   (runtime, ktx, compiler), WorkManager (work-runtime-ktx), Coil (compose),
   Vico (compose-m3), kotlinx-coroutines (core, android, test), junit4,
   kotlin-test, Turbine, Robolectric, androidx.test core/ext-junit.
   Pin to the latest stable versions resolvable at session time; record
   the pinned set in the handoff.
3. `core/build.gradle.kts`: `kotlin("jvm")`, serialization plugin, JVM
   target 17, deps limited to stdlib + serialization-json + test libs.
4. `app/build.gradle.kts`: `com.android.application`, compileSdk 36,
   targetSdk 36, minSdk 26, `applicationId = "ca.terradevop.openodo"`,
   literal `versionCode = 1` and `versionName = "0.1.0"` in
   `defaultConfig` (never computed), Java/Kotlin target 17 while Gradle
   runs on JDK 21, Compose enabled, Hilt + Room via KSP, Room schema
   export to `app/schemas/`,
   `testOptions.unitTests.isIncludeAndroidResources = true`, the
   reproducible-build settings from the architecture's F-Droid section
   (ArtProfile tasks disabled, `cruncherEnabled = false`, no shrinker,
   `isMinifyEnabled = true` on release with an initial
   `proguard-rules.pro` keeping the coroutines `ServiceLoader` classes),
   no `signingConfigs` block, depends on `:core`. All declared deps
   wired even though unused.
5. Dependency locking (`dependencyLocking { lockAllConfigurations() }`)
   in both modules and the buildscript; lockfiles generated with
   `--write-locks` and committed.
6. Minimal `app`: `MainActivity` with a single Compose `Text("OpenOdo")`
   in a Material 3 theme, Hilt `Application` class, manifest with **no**
   `INTERNET` permission. `app` must compile and
   `:app:testDebugUnitTest` must run (a trivial JVM test is fine here;
   no Robolectric test yet).
7. `core/src/test/kotlin/…/SmokeTest.kt`: one real test exercising a real
   `core` function (e.g. a `Metres.toKilometres()` value class round-trip).
8. `LICENSE` = verbatim GPL-3.0 text; SPDX header on every source file;
   `README.md` extended with build instructions (keep the factory lines).
9. `.gitignore` for Android/Gradle/IDE; `gradle.properties` with
   `org.gradle.jvmargs=-Xmx3g`, `android.useAndroidX=true`,
   `kotlin.code.style=official`.
10. Verify `gate/Dockerfile` (protected — read only) builds against this
    scaffold: run the two warm-up commands locally in order (online, then
    `--offline`) and report both results in the handoff. If the protected
    Dockerfile needs a change, that is an open question, not an edit.

## Scope: Out

- Any domain code beyond the smoke-test function.
- Room entities, DAOs, DI modules, navigation graph, screens.
- CI workflows, F-Droid metadata, signing config.
- Renaming the app or `applicationId`.

## Open questions (propose-first)

1. If any declared library is unavailable in a version compatible with
   the chosen Kotlin/AGP pair, propose the substitution (or omission with
   the slice it would first be needed in) in progress.md before changing
   the catalog.
2. If `gate/Dockerfile`'s `PLATFORM`/`BUILD_TOOLS`/cmdline-tools args are
   incompatible with what the scaffold needs, propose the exact new ARG
   values in progress.md.

## Acceptance

- From a clean clone with network: `./gradlew --no-daemon :core:test
  :app:testDebugUnitTest` exits 0; handoff shows the tail of the output.
- Immediately after, `./gradlew --offline --no-daemon :core:test
  :app:testDebugUnitTest` exits 0 (proves the lockfiles + cache are
  complete); handoff shows the tail.
- `git status` shows committed `gradle.lockfile` files for `core`, `app`,
  and `buildscript-gradle.lockfile` at root.
- `grep -R "INTERNET" app/src/main/AndroidManifest.xml` returns nothing.
- `grep -rn "jitpack\|signingConfigs\|storePassword" --include=*.kts .`
  returns nothing; `grep -n "versionCode\|versionName" app/build.gradle.kts`
  shows literal values.
- Every `*.kt`/`*.kts` file's first line is the SPDX header
  (`grep -L "SPDX-License-Identifier: GPL-3.0-only" $(git ls-files '*.kt')`
  prints nothing).
- Red proof for the smoke test: handoff shows it failing once against a
  deliberately wrong expected value, then passing.
- This slice is expected to end on the **designed gate refusal**
  (lockfile key changed; image not yet built). State that plainly in the
  handoff; do not treat it as failure.

## Session plan

- Session 1: items 1–6, 9; both warm-up commands green; handoff listing
  pinned versions. End cleanly on the designed refusal.
- Session 2 (only if needed): items 7, 8, 10 and any ruled open question.
