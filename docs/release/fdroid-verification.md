# F-Droid build verification

Repeatable procedure to verify the draft metadata (`fdroiddata.yml`) with
fdroidserver before opening the `New App` merge request.

## Prerequisites

- Docker with network access.
- A local copy of your `fdroiddata` fork with `metadata/ca.cyberengma.openodo.yml`
  filled in from `docs/release/fdroiddata.yml`.

## Steps

From a checkout that has the app source and the fdroiddata fork side by side:

```sh
# 1. Build the fdroidserver image (one time).
git clone https://gitlab.com/fdroid/fdroidserver.git
cd fdroidserver
docker build -t fdroidserver ./docker

# 2. Run verification against a scratch config copy of fdroiddata.
cd /path/to/fdroiddata
docker run --rm -v "$PWD:/repo:Z" fdroidserver readmeta
docker run --rm -v "$PWD:/repo:Z" fdroidserver lint ca.cyberengma.openodo
docker run --rm -v "$PWD:/repo:Z" fdroidserver build ca.cyberengma.openodo
```

`readmeta` and `lint` must report no errors; `build` must produce the APK from
the tagged commit without network access to third-party hosts other than the
declared `google()` and `mavenCentral()` repositories.

## Reproducibility check

Once the operator has signed a release, verify the built APK matches the
operator-published artifact with apksigcopier:

```sh
apksigcopier compare --unsigned \
  /path/to/fdroid-build.apk \
  /path/to/signed-release.apk
```

The two clean unsigned builds recorded in this repository (byte-identical
SHA-256, see the release handoff) are the local proof; apksigcopier confirms
F-Droid's server build reproduces the operator's published binary.