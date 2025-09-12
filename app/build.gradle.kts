plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.detekt)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.modulesGraphAssert)
    alias(libs.plugins.convention.androidApplication)
    alias(libs.plugins.convention.composeLibrary)
}

android {
    namespace = "ru.blays.hub"

    defaultConfig {
        applicationId = "ru.blays.hub"
        versionCode = libs.versions.projectVersionCode.get().toInt()
        versionName = libs.versions.projectVersionName.get()
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
    implementation(projects.core.packageManager)
    implementation(projects.utils.workerDsl)
    implementation(projects.utils.coilDsl)
}