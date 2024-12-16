pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "2.0.21")
            version("ktor", "3.0.2")
            version("kotlin-serialization", "1.7.3")
            version("kotest", "6.0.0.M1")
            version("kover", "0.9.0")
            version("kotlinx-datetime", "0.6.1")
            version("kotlinx-coroutines", "1.9.0")
            version("kotlinx-io", "0.6.0")
            version("resources", "0.9.0")
            version("slf4j", "2.0.16")
            version("dokka", "2.0.0")
            version("detekt", "1.23.7")
            version("ktlint", "12.1.2")
            version("publish", "2.0.0")

            plugin("kt-multiplatform", "org.jetbrains.kotlin.multiplatform").versionRef("kotlin")
            plugin("kt-serialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")
            plugin("kotest", "io.kotest.multiplatform").versionRef("kotest")
            plugin("kover", "org.jetbrains.kotlinx.kover").versionRef("kover")
            plugin("dokka", "org.jetbrains.dokka").versionRef("dokka")
            plugin("detekt", "io.gitlab.arturbosch.detekt").versionRef("detekt")
            plugin("ktlint", "org.jlleitschuh.gradle.ktlint").versionRef("ktlint")
            plugin("resources", "com.goncalossilva.resources").versionRef("resources")
            plugin("gradle-publish", "io.github.gradle-nexus.publish-plugin").versionRef("publish")

            library("ktor-core", "io.ktor", "ktor-client-core").versionRef("ktor")
            library("ktor-serialization", "io.ktor", "ktor-client-serialization").versionRef("ktor")
            library("ktor-content-negotiation", "io.ktor", "ktor-client-content-negotiation").versionRef("ktor")
            library("ktor-serialization-json", "io.ktor", "ktor-serialization-kotlinx-json").versionRef("ktor")
            library("ktor-logging", "io.ktor", "ktor-client-logging").versionRef("ktor")
            library("ktor-cio", "io.ktor", "ktor-client-cio").versionRef("ktor")
            library("ktor-js", "io.ktor", "ktor-client-js").versionRef("ktor")
            library("ktor-winhttp", "io.ktor", "ktor-client-winhttp").versionRef("ktor")
            library("ktor-darwin", "io.ktor", "ktor-client-darwin").versionRef("ktor")

            library("kt-serialization-json", "org.jetbrains.kotlinx", "kotlinx-serialization-json").versionRef("kotlin-serialization")
            library("kt-datetime", "org.jetbrains.kotlinx", "kotlinx-datetime").versionRef("kotlinx-datetime")
            library("kt-coroutines", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").versionRef("kotlinx-coroutines")
            library("kt-io", "org.jetbrains.kotlinx", "kotlinx-io-core").versionRef("kotlinx-io")

            library("kotest-core", "io.kotest", "kotest-assertions-core").versionRef("kotest")
            library("kotest-engine", "io.kotest", "kotest-framework-engine").versionRef("kotest")
            library("kotest-junit5", "io.kotest", "kotest-runner-junit5").versionRef("kotest")
            library("kotest-json", "io.kotest", "kotest-assertions-json").versionRef("kotest")

            library("resources", "com.goncalossilva", "resources").versionRef("resources")

            library("slf4j-simple", "org.slf4j", "slf4j-simple").versionRef("slf4j")

            bundle(
                "ktor-common",
                listOf("ktor-core", "ktor-serialization", "ktor-content-negotiation", "ktor-serialization-json")
            )
            bundle("kotest-common", listOf("kotest-core", "kotest-engine", "kotest-json"))
            bundle("kt-common", listOf("kt-serialization-json", "kt-datetime", "kt-coroutines"))
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "kault"
