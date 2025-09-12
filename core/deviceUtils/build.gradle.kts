plugins {
    alias(libs.plugins.convention.androidLibrary)
}

android {
    namespace = "ru.blays.hub.core.deviceUtils"

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
}

dependencies {
    // LibSu
    implementation(libs.libsu.core)

    // Shizuku
    implementation(libs.shizuku.api)

    // Project
    implementation(projects.core.packageManager)
}