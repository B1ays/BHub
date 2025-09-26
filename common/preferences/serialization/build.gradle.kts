plugins {
    alias(libs.plugins.convention.androidLibrary)
    alias(libs.plugins.kotlinx.serizalization)
}

android {
    namespace = "ru.blays.preferences.serialization"

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
    implementation(libs.kotlinx.coroutines.core)

    // Api
    api(projects.common.preferences.api)
    implementation(projects.common.preferences.impl)
    implementation(projects.common.preferences.extensions)
}