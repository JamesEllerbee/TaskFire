import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "lib"

        browser {
            commonWebpackConfig {
                outputFileName = "lib.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(project.projectDir.path)
                    }
                }
            }

            binaries.executable()
        }
    }

    jvm {
        jvmToolchain(19)
    }

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
        }
    }
}