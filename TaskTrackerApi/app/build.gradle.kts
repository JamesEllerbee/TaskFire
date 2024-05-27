import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "app"

        browser {
            commonWebpackConfig {
                outputFileName = "app.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(project.projectDir.path)
                    }
                }
            }

            binaries.executable()
        }
    }

    jvm("desktop") {
        jvmToolchain(19)
    }

    sourceSets {
        val desktopMain by getting
        val wasmJsMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation("org.jetbrains.compose.material:material-icons-extended:1.5.10-dev-wasm02")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2-wasm0")
            implementation("io.ktor:ktor-client-core:3.2.0")
            implementation("io.ktor:ktor-client-content-negotiation:3.2.0")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.2.0")

            implementation(project(":lib"))
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation("io.ktor:ktor-client-java:3.0.0-wasm2")
        }

        wasmJsMain.dependencies {
            implementation("io.ktor:ktor-client-js:3.2.0")
        }
    }
}

compose.experimental {
    web.application {}
}

compose.desktop {
    application {
        mainClass = "com.jamesellerbee.taskfire.app.desktop.MainKt"
    }
}