plugins {
    alias(libs.plugins.detekt)
    alias(libs.plugins.convention.androidLibrary)
    alias(libs.plugins.convention.composeLibrary)
}

android {
    namespace = "ru.blays.hub.core.ui"

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
            freeCompilerArgs.addAll(
                "-Xcontext-parameters",
                "-Xskip-prerelease-check"
            )
        }
    }

    buildTypes.forEach {
        it.buildConfigField(
            "String",
            "VERSION_CODE",
            "\"${libs.versions.projectVersionCode.get()}\""
        )
        it.buildConfigField(
            "String",
            "VERSION_NAME",
            "\"${libs.versions.projectVersionName.get()}\""
        )
    }
}

dependencies {
    // KotlinX
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.collections.immutable)

    // Constraint layout
    api(libs.androidx.constraintLayout)
    api(libs.androidx.constraintLayout.compose)

    // Compose debug
    debugApi(libs.androidx.compose.ui.tooling)
    debugApi(libs.androidx.compose.ui.test.manifest)
    debugApi(libs.androidx.compose.ui.tooling.preview)

    // Coil
    api(libs.coil)
    api(libs.coil.compose)
    api(libs.coil.svg)

    // Compose markdown
    api(libs.composeMarkdown)

    // MaterialKolor
    implementation(libs.materialKolor)

    // ComposeShadowsPlus
    implementation(libs.composeShadowsPlus)

    // Compose Shimmer
    implementation(libs.composeShimmer)

    // Koin
    api(libs.koin.core)
    api(libs.koin.compose)

    // Detekt plugins
    detektPlugins(libs.vkompose.detect)

    // Modules
    api(projects.core.domain)
    api(projects.core.packageManager.api)
}