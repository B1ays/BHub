plugins {
    alias(libs.plugins.convention.androidLibrary)
    alias(libs.plugins.kotlinx.serizalization)
}

android {
    namespace = "ru.blays.preferences.impl"

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
            optIn.add(
                "ru.blays.preferences.InternalPreferencesApi",
            )
        }
    }
}

dependencies {
    // KotlinX
    implementation(libs.kotlinx.serialization.json)

    // Api
    api(projects.common.preferences.api)
}