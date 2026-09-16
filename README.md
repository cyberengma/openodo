# OpenOdo

Privacy-first, offline, GPLv3 vehicle log for Android (fuel, expenses,
reminders with auto-reset, Drivvo/Fuelio import).

`docs/architecture.md` is the authority on what gets built; slice briefs
and progress live under `docs/slices/`.

## Build

Manual local build — no factory or docker gate needed:

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export ANDROID_HOME=$HOME/Android/Sdk        # or any machine SDK export
./gradlew --no-daemon :core:test :app:testDebugUnitTest          # online
./gradlew --offline --no-daemon :core:test :app:testDebugUnitTest # offline gate
```

Toolchain (pinned in `gradle/libs.versions.toml`; any change is a
deliberate catalog edit):

- JDK 21, Gradle wrapper 9.7.1, Kotlin 2.4.20 (K2), AGP 9.4.0, KSP 2.3.12
- compileSdk/targetSdk 37, minSdk 26 — bytecode target 17
- repositories: `google()` + `mavenCentral()` only; dependency locking on
  (committed lockfiles); no `INTERNET` permission; no secrets in the repo

```sh
./gradlew :app:assembleDebug   # debug APK
./gradlew :app:assembleRelease # unsigned, F-Droid-shaped (signed in CI later)
```

## Layout

```
core/            Kotlin/JVM domain: model, units, money, fuel, reminders,
                 portability — zero Android imports, exact arithmetic only
app/             Android app: Room, Compose UI, Hilt, WorkManager, SAF/zip
                 (slice 10). Depends on :core.
docs/            architecture, slices (brief/progress/handoffs), formats, release
fixtures/        import samples used by slice 05 tests (operator-supplied)
```

Package root: `ca.cyberengma.openodo`. GPL-3.0-only everywhere —
`LICENSE` at root, SPDX header on every source file.
