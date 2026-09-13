# Handoff — 01-scaffold, session 1

Builder: operator, manual local session (factory/docker gate not used).
Machine: enigma, Linux; JDK 21.0.12 (apt, /usr/lib/jvm/java-21-openjdk-amd64);
ANDROID_HOME=/home/enigma/Android/Sdk (platforms 35–37, build-tools 34–37).
Repo: /home/enigma/PDev/openodo (this directory is the origin for
github.com:cyberengma/openodo; push follows locally-made commits).

## What landed

- Gradle wrapper 9.7.1 (latest stable; AGP 9.4.0 requires Gradle 9.x).
- `settings.gradle.kts`: `:core`, `:app`; repositories `google()` +
  `mavenCentral()` only, `FAIL_ON_PROJECT_REPOS`.
- `gradle/libs.versions.toml`: every dependency and plugin for the whole
  program declared and pinned to latest stable (see table below).
- `core`: kotlin("jvm") + serialization, JVM target 17 (toolchain), deps
  exactly stdlib + serialization-json; junit4 + kotlin-test only in tests.
- `app`: AGP 9.4.0 (built-in Kotlin 2.4.20), Kotlin 2.4.20 (K2) via
  catalog pins, KSP 2.3.12, Hilt 2.60.1, Room 2.8.5 (compiler via KSP,
  schemas → `app/schemas/`), Compose BOM 2026.09.00 + Material 3,
  Coil 2.7.0, Vico compose-m3 3.3.1, WorkManager-ktx 2.11.2,
  coroutines 1.11.0; all wired even though unused yet.
  compileSdk/targetSdk 37, minSdk 26, versionCode = 1, versionName =
  "0.1.0" literal; no INTERNET permission; no signing config;
  release minified with coroutines ServiceLoader keeps; PNG crunching +
  ArtProfile tasks disabled at task level (removed from the AGP 9 DSL).
  Robolectric/Turbine/androidx.test in unit test deps for later slices.
- `core/src/main/.../core/units/Metres.kt`: first real `core` value
  (slice 02 extends the file/module).
- GPL-3.0 LICENSE at root; SPDX header on every `.kt`/`.kts`.
- Dependency locking on: buildscript + settings (root), core, app —
  `gradle.lockfile` files committed.
- Minimal `app`: `MainActivity` Compose `Text("OpenOdo")` in a Material 3
  theme, `@HiltAndroidApp` application class, trivial app-module JVM test.

## Pinned versions (latest stable at 2026-09-13)

| artifact | version |
|---|---|
| gradle wrapper | 9.7.1 |
| com.android.application (AGP) | 9.4.0 |
| kotlin (KGP / kotlin.jvm) | 2.4.20 |
| com.google.devtools.ksp | 2.3.12 |
| org.jetbrains.kotlin.plugin.serialization | 2.4.20 |
| org.jetbrains.kotlin.plugin.compose | 2.4.20 |
| junit:junit | 4.13.2 |
| kotlinx-serialization-json | 1.11.0 |
| kotlinx-coroutines core/android/test | 1.11.0 |
| compose BOM | 2026.09.00 |
| activity-compose | 1.13.0 |
| navigation-compose | 2.10.1 |
| lifecycle-viewmodel-compose | 2.11.0 |
| hilt-android / compiler / plugin | 2.60.1 |
| room runtime/ktx/compiler/plugin | 2.8.5 |
| work-runtime-ktx | 2.11.2 |
| coil-compose | 2.7.0 |
| com.patrykandpatrick.vico:compose-m3 | 3.3.1 |
| turbine | 1.2.1 |
| robolectric | 4.17 |
| androidx.test core / ext.junit | 1.7.0 / 1.3.0 |

Not available at session time (checked central + google + solr):
`com.google.dagger:hilt-navigation-compose` — omitted, see progress.md.

## Commands + results

1. RED proof (deliberately wrong expected value in MetresTest):

```
> Task :core:test FAILED
MetresTest > toKilometres returns whole kilometres FAILED
    java.lang.AssertionError: expected:<1598> but was:<1599>
BUILD FAILED
```

2. `./gradlew --no-daemon --write-locks :core:test :app:testDebugUnitTest`

```
> Task :app:testDebugUnitTest
BUILD SUCCESSFUL in 1m 40s
41 actionable tasks: 34 executed, 7 up-to-date
EXIT=0
```

3. `./gradlew --offline --no-daemon :core:test :app:testDebugUnitTest`

```
> Task :app:testDebugUnitTest
BUILD SUCCESSFUL in 1m 6s
41 actionable tasks: 39 executed, 2 up-to-date
EXIT=0
```

Tests: AppSmokeTest 1/1 green, MetresTest 2/2 green (junit xml,
0 failures / 0 errors).

## Gate status

Expected designed refusal does not fire from here: lockfile key
(`gradle/libs.versions.toml`) and the local environment carry the same
pinned set; there is no further dep delta in later slices unless a new
library is proposed as an open question (which ends the session per
the contract).
