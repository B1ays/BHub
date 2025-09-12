package tech.blays.gradle

import com.android.build.api.dsl.ApplicationBuildType
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.VariantDimension
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.configure

/**
 * Общая конфигурация android
 */
internal fun Project.androidCommon(
    action: CommonExtension<*, *, *, *, *, *>.() -> Unit
) {
    val extension = extensions.findByType(CommonExtension::class.java)
    extension?.action()
}

/**
 * Конфигурация android приложения
 */
internal fun Project.androidApplication(
    action: ApplicationExtension.() -> Unit
) = extensions.configure(action)

/**
 * Конфигурация android библиотеки
 */
internal fun Project.androidLibrary(
    action: LibraryExtension.() -> Unit
) = extensions.configure(action)

fun DependencyHandlerScope.implementation(dependencyNotation: Any) {
    add("implementation", dependencyNotation)
}
fun DependencyHandlerScope.debugImplementation(dependencyNotation: Any) {
    add("debugImplementation", dependencyNotation)
}
fun DependencyHandlerScope.api(dependencyNotation: Any) {
    add("api", dependencyNotation)
}
fun DependencyHandlerScope.compileOnly(dependencyNotation: Any) {
    add("compileOnly", dependencyNotation)
}

inline fun <reified T: Any> VariantDimension.buildConfigField(name: String, value: T) {
    val value = when(value) {
        is String -> "\"$value\""
        else -> value.toString()
    }
    buildConfigField(T::class.simpleName.orEmpty(), name, value)
}

inline fun NamedDomainObjectContainer<ApplicationBuildType>.release(action: Action<in ApplicationBuildType>) =
    getByName("release", action)

inline fun NamedDomainObjectContainer<ApplicationBuildType>.debug(action: Action<in ApplicationBuildType>) =
    getByName("debug", action)