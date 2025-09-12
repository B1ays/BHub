plugins {
    alias(libs.plugins.convention.androidLibrary)
}

android {
    namespace = "ru.blays.hub.core.packageNanager.nonRoot"

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
    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.core.coroutines)

    // Api module
    implementation(projects.core.packageManager)
}