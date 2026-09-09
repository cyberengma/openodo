# OpenOdo offline gate image.
# Build from the repo root AFTER slice 01 has landed (and again whenever
# gradle/libs.versions.toml changes):
#   docker build -f gate/Dockerfile -t factory-android-gate:openodo .
# The gate runs the contract's test command in this image with --network=none.
# Everything Gradle needs offline is warmed into GRADLE_USER_HOME here.

FROM eclipse-temurin:17-jdk

ARG CMDLINE_TOOLS_ZIP=commandlinetools-linux-11076708_latest.zip
ARG PLATFORM=android-36
ARG BUILD_TOOLS=36.0.0

ENV ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    GRADLE_USER_HOME=/opt/gradle-home \
    GRADLE_OPTS="-Dorg.gradle.daemon=false"

RUN apt-get update && apt-get install -y --no-install-recommends unzip curl git ca-certificates \
 && rm -rf /var/lib/apt/lists/*

RUN mkdir -p ${ANDROID_HOME}/cmdline-tools \
 && curl -fsSL -o /tmp/tools.zip https://dl.google.com/android/repository/${CMDLINE_TOOLS_ZIP} \
 && unzip -q /tmp/tools.zip -d ${ANDROID_HOME}/cmdline-tools \
 && mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
 && rm /tmp/tools.zip
ENV PATH=${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${PATH}

RUN yes | sdkmanager --licenses >/dev/null \
 && sdkmanager "platform-tools" "platforms;${PLATFORM}" "build-tools;${BUILD_TOOLS}"

# Warm-up: run the exact gate command once WITH network so every artifact
# (Gradle distribution, plugins, dependencies, KSP, Robolectric jars if
# declared) lands in GRADLE_USER_HOME. The source tree is then discarded;
# the gate re-clones fresh.
WORKDIR /warmup
COPY . /warmup
RUN ./gradlew --no-daemon :core:test :app:testDebugUnitTest \
 && ./gradlew --no-daemon --offline :core:test :app:testDebugUnitTest \
 && rm -rf /warmup

WORKDIR /work
