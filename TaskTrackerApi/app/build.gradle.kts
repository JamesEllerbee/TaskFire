plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
    application
}

group = "com.jamesellerbee.taskfire.trasktrackerapi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:2.3.8")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.8")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.3.8")
    implementation("io.ktor:ktor-server-default-headers-jvm:2.3.8")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.8")
    implementation("io.ktor:ktor-server-call-logging:2.3.8")
    implementation("io.ktor:ktor-server-cors:2.3.8")
    implementation("io.ktor:ktor-server-auth:2.3.8")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.8")
    implementation("io.ktor:ktor-network-tls-certificates:2.3.8")
    implementation("io.ktor:ktor-server-openapi:2.3.8")
    implementation("org.mindrot:jbcrypt:0.4")

    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.8")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("org.slf4j:slf4j-api:2.0.11")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.22.1")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")

    implementation("org.jetbrains.exposed:exposed-core:0.47.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.47.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.47.0")

    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
}

kotlin {
    jvmToolchain(19)
}

application {
    mainClass = "com.jamesellerbee.taskfire.tasktrackerapi.app.MainKt"
}

tasks {
    shadowJar {
        archiveBaseName.set("taskfireapi")
        archiveClassifier.set("")
        archiveVersion.set("")
    }
}