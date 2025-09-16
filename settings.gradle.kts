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
include(":core:data")
include(":core:package-manager:api")
include(":core:package-manager:non-root")
include(":core:package-manager:root")
include(":core:package-manager:shizuku")
include(":core:module-manager")
include(":core:downloader")
include(":core:preferences")
include(":core:device-utils")
include(":core:logger")

// Common
include(":common:logger")

// Utils
include(":utils:workerDsl")
include(":utils:coilDsl")

