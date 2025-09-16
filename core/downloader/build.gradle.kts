plugins {
    alias(libs.plugins.kotlinx.serizalization)
    alias(libs.plugins.convention.androidLibrary)
}

android {
    namespace = "ru.blays.hub.core.downloader"

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
    // AndroidX
    implementation(libs.androidx.work.runtime)

    // KotlinX
    implementation(libs.kotlinx.serialization.json)

    // OkHttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.coroutines)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.work)

    // project
    implementation(projects.utils.workerDsl)
}