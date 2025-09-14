plugins {
    alias(libs.plugins.kotlinx.serizalization)
    alias(libs.plugins.convention.androidLibrary)
}

android {
    namespace = "ru.blays.hub.core.modulemanager"

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
    // KotlinX
    implementation(libs.kotlinx.serialization.json)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.core.coroutines)

    // Libsu
    implementation(libs.libsu.core)
}