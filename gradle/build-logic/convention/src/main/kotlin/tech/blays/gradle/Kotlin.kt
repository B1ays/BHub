package tech.blays.gradle

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

context(commonExtension: CommonExtension<*, *, *, *, *, *>)
internal fun Project.configureKotlinAndroid() {
    commonExtension.apply {
        compileOptions {
            val javaVersion = JavaVersion.toVersion(Versions.JAVA_VERSION)
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }
    }
    configureKotlin()
}

internal fun Project.configureKotlinJvm() {
    extensions.configure<JavaPluginExtension> {
        val javaVersion = JavaVersion.toVersion(Versions.JAVA_VERSION)
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    configureKotlin()
}

internal fun Project.kotlinCompilerOptions(block: KotlinJvmCompilerOptions.() -> Unit) {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions(block)
    }
}

private fun Project.configureKotlin() {
    kotlinCompilerOptions {
        jvmTarget.set(
            JvmTarget.fromTarget(Versions.JAVA_VERSION)
        )
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
            "-Xskip-prerelease-check",
            "-Xannotation-default-target=param-property",
            "-XXLanguage:+ExplicitBackingFields",
        )
    }
}