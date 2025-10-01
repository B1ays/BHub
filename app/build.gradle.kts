import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.detekt)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.modulesGraphAssert)
    alias(libs.plugins.convention.androidApplication)
    alias(libs.plugins.convention.composeLibrary)
}

val keystoreProps = properties(rootProject.file("keystore.properties"))

android {
    namespace = "ru.blays.hub"

    signingConfigs {
        create("main") {
            keyAlias = keystoreProps.getProperty("keyAlias")
            keyPassword = keystoreProps.getProperty("keyPassword")
            storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
            storePassword = keystoreProps.getProperty("storePassword")

            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    defaultConfig {
        applicationId = "ru.blays.hub"
        versionCode = libs.versions.projectVersionCode.get().toInt()
        versionName = libs.versions.projectVersionName.get()

        signingConfig = signingConfigs["main"]
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            ndk {
                abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
            }
        }
        debug {
            //applicationIdSuffix = ".debug"
            ndk {
                //noinspection ChromeOsAbiSupport
                abiFilters += listOf("arm64-v8a", "x86_64")
            }
            signingConfig = signingConfigs["main"]
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.work.runtime)

    // Google
    implementation(libs.material)

    // Decompose
    implementation(libs.decompose)
    implementation(libs.decompose.compose)
    implementation(libs.essenty)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.work)

    // Hidden API bypass
    implementation(libs.hiddenApi.bypass)

    // Detekt plugins
    detektPlugins(libs.vkompose.detect)

    // LibSu
    implementation(libs.libsu.core)

    // Modules
    implementation(projects.core.network)
    implementation(projects.core.ui)
    implementation(projects.core.packageManager.api)
    implementation(projects.common.preferences.api)
    implementation(projects.common.preferences.extensions)
    implementation(projects.common.preferences.default)
    implementation(projects.common.utils)
}

private fun properties(file: File): Properties {
    return Properties().apply {
        load(file.reader())
    }
}