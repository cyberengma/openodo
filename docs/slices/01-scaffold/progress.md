# Progress — 01-scaffold

- 2026-09-13, session 1 (operator, manual build, local): scaffold complete.
  Two-module project (`:core`, `:app`) builds and tests green locally:
  - online warm-up `./gradlew --no-daemon --write-locks :core:test :app:testDebugUnitTest` → EXIT 0
  - offline `./gradlew --offline --no-daemon :core:test :app:testDebugUnitTest` → EXIT 0
  - red proof: smoke test shown failing against a deliberately wrong
    expected value (`expected:<1598> but was:<1599>`), then fixed green.
  - lockfiles committed: root `buildscript-gradle.lockfile`,
    `settings-gradle.lockfile`, `app/gradle.lockfile`, `core/gradle.lockfile`.
  All acceptance greps pass (no INTERNET, no jitpack/signingConfigs/
  storePassword, literal versionCode/versionName, SPDX on every kt/kts).

  Pinned set (latest stable resolvable at session time): see
  `handoffs/session-1.md` and `gradle/libs.versions.toml`.

  Deviations from architecture v0.1 table (notes + proposals, no catalog
  change without a ruling):
  1. AGP is now versioned in the 9.x line; latest stable = 9.4.0.
     `com.google.dagger:hilt-android:2.60.1` hard-requires AGP ≥ 9.0.0,
     so the latest-stable pin lands on AGP 9.4.0 (table says "AGP 8.x").
     Consequences taken in session 1: AGP 9.x bundles Kotlin support
     (`org.jetbrains.kotlin.android` plugin no longer applied in `:app`),
     and the Gradle wrapper is 9.7.1 (AGP 9.4.0 needs Gradle 9.x; brief
     item "latest stable 8.x" wrapper cannot host AGP 9).
  2. compileSdk/targetSdk raised 36 → 37: compose 2026.09.00,
     navigation-compose 2.10.1, vico 3.3.1 all require compileSdk ≥ 37.
     minSdk stays 26.
  3. `com.google.dagger:hilt-navigation-compose` not published anywhere
     (checked central + google maven + search index). Omitted from the
     catalog; slice 07 can drop in a six-line local `hiltViewModel()`
     shim if wanted.
