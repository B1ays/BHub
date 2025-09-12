import com.google.protobuf.gradle.proto

plugins {
    alias(libs.plugins.google.protobufs)
    alias(libs.plugins.convention.androidLibrary)
}

android {
    namespace = "ru.blays.hub.core.preferences"

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
    sourceSets {
        named("main") {
            proto {
                srcDir("src/main/proto")
            }
        }
    }
    kotlin {
        compilerOptions {
            freeCompilerArgs.add("-Xcontext-parameters")
        }
    }
}

dependencies {
    // Datastore
    api(libs.androidx.datastore.proto)

    // Protobuf
    api(libs.protobuf.javalite)
    api(libs.protobuf.kotlinlite)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.core.coroutines)
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("java") {
                    option("lite")
                }
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}