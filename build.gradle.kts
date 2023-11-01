import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    embeddedKotlin("multiplatform")
    embeddedKotlin("plugin.serialization")
    id("org.jetbrains.dokka") version "1.9.10"
    id("io.gitlab.arturbosch.detekt") version "1.23.1"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
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
    js {
        nodejs()
        binaries.library()
    }

    val hostOs = System.getProperty("os.name")
    val isArm64 = System.getProperty("os.arch") == "aarch64"
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" && isArm64 -> macosArm64("native")
        hostOs == "Mac OS X" && !isArm64 -> macosX64("native")
        hostOs == "Linux" && isArm64 -> linuxArm64("native")
        hostOs == "Linux" && !isArm64 -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {

        all {
            languageSettings {
                optIn("kotlin.contracts.ExperimentalContracts")
            }
        }

        val ktSerializationVersion = "1.6.0"
        val ktorVersion = "2.3.5"

        val commonMain by getting {

            dependencies {
                api("io.ktor:ktor-client-core:$ktorVersion")
                api("io.ktor:ktor-client-serialization:$ktorVersion")
                api("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                api("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:$ktSerializationVersion")
                api("io.ktor:ktor-client-logging:2.3.5")
            }
        }
        val commonTest by getting {

            val kotestVersion = "5.7.2"

            dependencies {
                implementation(kotlin("test"))
                implementation("io.kotest:kotest-assertions-core:$kotestVersion")
            }
        }

        val jvmMain by getting {
            dependsOn(commonMain)

            dependencies {
                implementation("org.slf4j:slf4j-simple:2.0.9")
                implementation("io.ktor:ktor-client-apache5:$ktorVersion")
            }
        }
        val jvmTest by getting

        val jsMain by getting {
            dependsOn(commonMain)
        }
        val jsTest by getting

        val nativeMain by getting {
            dependsOn(commonMain)
        }
        val nativeTest by getting
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
