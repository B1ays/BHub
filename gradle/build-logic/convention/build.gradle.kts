plugins {
    `kotlin-dsl` version "6.3.0"
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        registerForClass(
            name = "androidApplication",
            id = "tech.blays.android.application",
            implementationClass = "tech.blays.gradle.AndroidApplicationPlugin"
        )
        registerForClass(
            name = "androidLibrary",
            id = "tech.blays.android.library",
            implementationClass = "tech.blays.gradle.AndroidLibraryPlugin"
        )
        registerForClass(
            name = "jvmLibrary",
            id = "tech.blays.jvm.library",
            implementationClass = "tech.blays.gradle.JvmLibraryPlugin"
        )
        registerForClass(
            name = "composeLibrary",
            id = "tech.blays.compose.library",
            implementationClass = "tech.blays.gradle.ComposePlugin"
        )
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

private fun NamedDomainObjectContainer<PluginDeclaration>.registerForClass(
    name: String,
    id: String,
    implementationClass: String
) = register(name) {
    this.id = id
    this.implementationClass = implementationClass
}