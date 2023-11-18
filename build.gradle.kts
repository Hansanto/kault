import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    libs.plugins.run {
        alias(kt.multiplatform)
        alias(kt.serialization)
        alias(kotest)
        alias(dokka)
        alias(detekt)
        alias(ktlint)
        alias(resources)
    }
    `maven-publish`
}

repositories {
    mavenCentral()
}

detekt {
    ignoreFailures = System.getenv("DETEKT_IGNORE_FAILURES")?.toBooleanStrictOrNull() ?: false
    config.from(file("config/detekt/detekt.yml"))
    reportsDir = file("reports/detekt")
}

kotlin {
    explicitApi = org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Strict

    jvm {
        jvmToolchain(8)
        withJava()
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }
    js(IR) {
        nodejs()
        binaries.library()
        useCommonJs()
        generateTypeScriptDefinitions()
    }

    val hostOs = System.getProperty("os.name")
    val isMacOs = hostOs == "Mac OS X"
    val isLinux = hostOs == "Linux"
    val isWindows = hostOs.startsWith("Windows")

    val isArm64 = System.getProperty("os.arch") == "aarch64"
    when {
        isMacOs && isArm64 -> macosArm64("native")
        isMacOs && !isArm64 -> macosX64("native")
        isLinux && isArm64 -> linuxArm64("native")
        isLinux && !isArm64 -> linuxX64("native")
        isWindows -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.contracts.ExperimentalContracts")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
            }
        }

        val commonMain by getting {
            dependencies {
                api(libs.bundles.ktor.common)
                api(libs.bundles.kt.common)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.bundles.kotest.common)
                implementation(libs.ktor.logging)
                implementation(libs.resources)
            }
        }

        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(libs.ktor.cio)
                implementation(libs.slf4j.simple)
                implementation(libs.kotest.junit5)
            }
        }

        val jsMain by getting
        val jsTest by getting {
            dependencies {
                implementation(libs.ktor.js)
            }
        }

        val nativeMain by getting
        val nativeTest by getting {
            dependencies {
                if (isWindows) {
                    implementation(libs.ktor.winhttp)
                } else if (isMacOs) {
                    implementation(libs.ktor.darwin)
                } else {
                    implementation(libs.ktor.cio)
                }
            }
        }
    }
}

val dokkaOutputDir = "${rootProject.projectDir}/dokka"
tasks {
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        reporters {
            reporter(ReporterType.HTML)
            reporter(ReporterType.CHECKSTYLE)
        }
    }

    withType<org.jlleitschuh.gradle.ktlint.tasks.GenerateReportsTask> {
        reportsOutputDirectory.set(file("reports/klint/$name"))
    }

    withType<Detekt>().configureEach {
        jvmTarget = "1.8"

        reports {
            html.required.set(true)
            xml.required.set(true)
            txt.required.set(false)
            sarif.required.set(false)
            md.required.set(false)
        }
    }

    withType<DetektCreateBaselineTask>().configureEach {
        jvmTarget = "1.8"
    }

    clean {
        delete(dokkaOutputDir)
    }

    val deleteDokkaOutputDir by register<Delete>("deleteDokkaOutputDirectory") {
        group = "documentation"
        delete(dokkaOutputDir)
    }

    dokkaHtml.configure {
        dependsOn(deleteDokkaOutputDir)
        outputDirectory.set(file(dokkaOutputDir))
    }
}
