plugins {
    alias(libs.plugins.convention.androidLibrary)
    alias(libs.plugins.refineTools)
}

android {
    namespace = "ru.blays.hub.core.packgeManager.shizuku"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        aidl = true
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
}

dependencies {
    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.core.coroutines)

    // Shizuku
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)

    // RefineTools
    implementation(libs.refineTools.runtime)

    // Api module
    implementation(projects.core.packageManager.api)
}