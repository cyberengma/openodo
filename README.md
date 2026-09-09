# OpenOdo

Privacy-first, offline, GPLv3 vehicle log for Android (fuel, expenses, reminders with auto-reset, Drivvo/Fuelio import).
Built by the factory: `docs/architecture.md` is the authority; work happens in slices under `docs/slices/`.
Gated offline by `./gradlew --offline --no-daemon :core:test :app:testDebugUnitTest` inside the image from `gate/Dockerfile`.
Workers do not run git or touch protected paths (see `factory.toml`); questions go to the slice's `progress.md`.
