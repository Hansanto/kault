pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories { mavenCentral() }
    versionCatalogs {
        create("ktorLibs") {
            from("io.ktor:ktor-version-catalog:3.2.1")
        }

        create("libs") {
            version("kotlin", "2.2.0")
            version("kotlin-serialization", "1.9.0")
            version("kotest", "6.0.0.M1")
            version("kover", "0.9.1")
            version("kotlinx-datetime", "0.7.0-0.6.x-compat")
            version("kotlinx-coroutines", "1.10.2")
            version("kotlinx-io", "0.8.0")
            version("resources", "0.10.0")
            version("slf4j", "2.0.17")
            version("dokka", "2.0.0")
            version("detekt", "1.23.8")
            version("ktlint", "13.0.0")
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

            bundle("kotest-common", listOf("kotest-core", "kotest-engine", "kotest-json"))
            bundle("kt-common", listOf("kt-serialization-json", "kt-datetime", "kt-coroutines"))
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "kault"
