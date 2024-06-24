import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.kotlinx.serizalization)
}

dependencies {
    // AndroidX
    implementation(libs.androidx.annotation.jvm)

    // KotlinX
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)

    // OkHttp
    api(libs.okhttp)
    api(libs.okhttp.coroutines)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.core.coroutines)
}

java {
    sourceCompatibility = JavaVersion.VERSION_18
    targetCompatibility = JavaVersion.VERSION_18
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll("-Xcontext-receivers", "-Xskip-prerelease-check")
    }
}