plugins {
    alias(libs.plugins.kotlinx.serizalization)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.convention.androidLibrary)
}

android {
    namespace = "ru.blays.hub.core.data"

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

    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.annotationProcessor)

    // KotlinX
    implementation(libs.kotlinx.serialization.json)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.core.coroutines)
}