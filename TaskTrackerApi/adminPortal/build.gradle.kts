import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "adminPortal"

        browser {
            commonWebpackConfig {
                outputFileName = "adminPortal.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(project.projectDir.path)
                    }
                }
            }

            binaries.executable()
        }
    }

    sourceSets {
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
    }
}

compose.experimental {
    web.application {}
}