plugins {
    alias(libs.plugins.kotlinx.serizalization)
    alias(libs.plugins.convention.jvmLibrary)
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
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
            "-Xskip-prerelease-check"
        )
    }
}