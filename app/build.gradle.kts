// SPDX-License-Identifier: GPL-3.0-only
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
}

dependencyLocking {
    lockAllConfigurations()
}

// Release signing is optional and driven by keystore.properties (git-ignored)
// or CI environment variables. When neither is present the release stays
// unsigned (the GitHub workflow signs the APK with apksigner afterwards).
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) keystorePropertiesFile.inputStream().use { load(it) }
}

fun signingValue(fileKey: String, envKey: String): String? =
    (keystoreProperties.getProperty(fileKey) ?: System.getenv(envKey))?.takeIf { it.isNotBlank() }

val releaseStoreFilePath = signingValue("storeFile", "RELEASE_KEYSTORE")

android {
    namespace = "ca.cyberengma.openodo"
    compileSdk = 37

    defaultConfig {
        applicationId = "ca.cyberengma.openodo"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseStoreFilePath != null) {
            create("release") {
                storeFile = file(releaseStoreFilePath)
                storePassword = signingValue("storePassword", "RELEASE_STORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "RELEASE_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (releaseStoreFilePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

tasks.whenTaskAdded {
    if (name.contains("Crunch") || name.contains("ArtProfile") || name.contains("ComposeMapping")) {
        enabled = false
    }
}

// AGP 9.4.0 emits mapping.prt to outputs/mapping/release but bundleRelease still
// expects mapping.txt there. Copy the real R8 mapping so AAB packaging succeeds.
tasks.register<Copy>("copyReleaseMapping") {
    dependsOn("minifyReleaseWithR8")
    from(layout.buildDirectory.dir("intermediates/mapping/release/minifyReleaseWithR8"))
    include("mapping.txt")
    into(layout.buildDirectory.dir("outputs/mapping/release"))
}

tasks.matching { it.name == "packageReleaseBundle" }.configureEach {
    dependsOn("copyReleaseMapping")
}

dependencies {
    implementation(project(":core"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.material3)
    implementation(libs.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.work.runtime.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
}
