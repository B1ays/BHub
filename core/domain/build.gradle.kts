plugins {
    alias(libs.plugins.kotlinx.serizalization)
    alias(libs.plugins.convention.androidLibrary)
}

android {
    namespace = "ru.blays.hub.core.domain"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xcontext-parameters",
                "-XXLanguage:+ExplicitBackingFields",
                "-Xskip-prerelease-check"
            )
        }
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(libs.androidx.compose.runtime)

    // KotlinX
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.serialization.json)

    // Decompose
    api(libs.decompose)
    api(libs.decompose.compose)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.work)

    // Shizuku
    implementation(libs.shizuku.api)

    // Modules
    implementation(projects.core.network)
    implementation(projects.core.packageManager.api)
    implementation(projects.core.packageManager.nonRoot)
    implementation(projects.core.packageManager.root)
    implementation(projects.core.packageManager.shizuku)
    implementation(projects.core.moduleManager)
    implementation(projects.core.downloader)
    //api(projects.core.preferences)
    implementation(projects.core.deviceUtils)
    implementation(projects.core.logger)
    implementation(projects.core.data)

    // Common
    implementation(projects.common.utils)

    implementation(projects.common.preferences.api)
    implementation(projects.common.preferences.default)
    implementation(projects.common.preferences.extensions)

    // Extensions
    implementation(projects.utils.workerDsl)
}