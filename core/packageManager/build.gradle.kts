plugins {
    alias(libs.plugins.convention.androidLibrary)
}

android {
    namespace = "ru.blays.hub.core.packageManager"

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
    // Hidden Api
    api(libs.hiddenApi.stub)

    // Koin
    implementation(libs.koin.core)
}