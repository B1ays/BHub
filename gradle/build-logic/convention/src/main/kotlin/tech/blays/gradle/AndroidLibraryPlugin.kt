package tech.blays.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.android.library")
            apply("org.gradle.android.cache-fix")
            apply("org.jetbrains.kotlin.android")
        }
        androidLibrary {
            compileSdk = Versions.COMPILE_SDK

            defaultConfig {
                minSdk = Versions.MIN_SDK
            }

            buildFeatures {
                buildConfig = true
            }

            configureKotlinAndroid()
        }
        dependencies {
            implementation(libs.findLibrary("androidx-core-ktx").get())
            implementation(libs.findLibrary("androidx-lifecycle-runtime-ktx").get())
            implementation(libs.findLibrary("androidx-appcompat").get())
        }
    }
}