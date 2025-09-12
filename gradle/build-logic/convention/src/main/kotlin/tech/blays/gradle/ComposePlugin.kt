package tech.blays.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class ComposePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.plugin.compose")
        }
        dependencies {
            // Common libs
            implementation(platform(libs.findLibrary("androidx-compose-bom").get()))
            implementation(libs.findLibrary("androidx-compose-ui").get())
            implementation(libs.findLibrary("androidx-compose-ui-graphics").get())
            implementation(libs.findLibrary("androidx-compose-ui-tooling-preview").get())
            implementation(libs.findLibrary("androidx-compose-material3").get())
            debugImplementation(libs.findLibrary("androidx-compose-ui-tooling").get())
        }
        androidCommon {
            buildFeatures {
                compose = true
            }
        }
        kotlinCompilerOptions {
            freeCompilerArgs.addAll(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:featureFlag=OptimizeNonSkippingGroups",
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:featureFlag=PausableComposition",
            )
        }
    }
}