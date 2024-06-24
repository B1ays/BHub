import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.detekt)
}

android {
    namespace = "ru.blays.hub.core.ui"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        buildConfig = true
        compose = true
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

    // KotlinX
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.collections.immutable)

    // Compose
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.androidx.compose.material3)
    api(libs.androidx.activity.compose)

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
    api(projects.core.logic)
}

composeCompiler {
    enableStrongSkippingMode = true
    enableNonSkippingGroupOptimization = true
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll("-Xcontext-receivers", "-Xskip-prerelease-check")
    }
}