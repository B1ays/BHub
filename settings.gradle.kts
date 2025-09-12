enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("gradle/build-logic")

    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }
}

rootProject.name = "BHub"

// App
include(":app")

// Core
include(":core:domain")
include(":core:ui")
include(":core:network")
include(":core:packageManager")
include(":core:packageManager:nonRoot")
include(":core:packageManager:root")
include(":core:packageManager:shizuku")
include(":core:moduleManager")
include(":core:downloader")
include(":core:preferences")
include(":core:deviceUtils")
include(":core:logger")

// Utils
include(":utils:workerDsl")
include(":core:data")
include(":utils:coilDsl")
