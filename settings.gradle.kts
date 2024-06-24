enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
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
include(":app")
include(":core")
include(":core:network")
include(":core:ui")
//include(":core:ui:bom")
include(":core:packageManager")
include(":core:packageManager:nonRoot")
include(":core:packageManager:root")
include(":core:packageManager:shizuku")
include(":core:logic")
include(":core:moduleManager")
include(":core:downloader")
include(":core:preferences")
include(":core:deviceUtils")
include(":core:logger")
include(":utils")
include(":utils:workerDsl")
include(":core:data")
include(":utils:coilDsl")
