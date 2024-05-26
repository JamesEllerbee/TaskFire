pluginManagement {
    plugins {
        kotlin("jvm") version "1.9.22"
        kotlin("plugin.serialization") version "1.9.22"
        id("com.github.johnrengelman.shadow") version "8.1.1"

        kotlin("multiplatform") version "1.9.23"
        id("org.jetbrains.compose") version "1.6.10-dev1580"
    }

    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    }
}

rootProject.name = "TaskTrackerApi"
include(":api", ":app", ":lib")