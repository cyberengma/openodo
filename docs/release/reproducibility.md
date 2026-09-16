# Reproducible build proof

Command (offline, no network):

```sh
./gradlew --offline --no-daemon clean :app:assembleRelease
```

Two consecutive clean builds produce a byte-identical unsigned release APK.

SHA-256 (both runs):

```
d4685e3bcd0d9fec1830e9822272a4b5a9a82b9e38722df0736dfbd3306202b5
```

Artifact: `app/build/outputs/apk/release/app-release-unsigned.apk`

The signed APK is produced only by CI at a release tag using the operator's
keystore (see `.github/workflows/release.yml`); apksigcopier confirms F-Droid's
server build reproduces that signed artifact (see `fdroid-verification.md`).