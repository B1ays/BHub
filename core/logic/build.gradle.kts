import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlinx.serizalization)
}

android {
    namespace = "ru.blays.hub.core.logic"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    kotlinOptions {
        jvmTarget = "18"
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(libs.androidx.compose.runtime)

    // KotlinX
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.serialization.json)

    // Decompose
    api(libs.decompose)
    api(libs.decompose.compose)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.core.coroutines)
    implementation(libs.koin.work)

    // Shizuku
    implementation(libs.shizuku.api)

    // Modules
    implementation(projects.core.network)
    implementation(projects.core.packageManager)
    implementation(projects.core.packageManager.nonRoot)
    implementation(projects.core.packageManager.root)
    implementation(projects.core.packageManager.shizuku)
    implementation(projects.core.moduleManager)
    implementation(projects.core.downloader)
    api(projects.core.preferences)
    implementation(projects.core.deviceUtils)
    implementation(projects.core.logger)
    implementation(projects.core.data)

    // Extensions
    implementation(projects.utils.workerDsl)
}

kotlin {
    sourceSets.all {
        languageSettings {
            languageVersion = "2.0"
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll("-Xcontext-receivers", "-XXLanguage:+ExplicitBackingFields", "-Xskip-prerelease-check")
    }
}