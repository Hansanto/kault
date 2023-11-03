import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    embeddedKotlin("multiplatform")
    embeddedKotlin("plugin.serialization")
    id("io.kotest.multiplatform") version "5.7.2"
    id("org.jetbrains.dokka") version "1.9.10"
    id("io.gitlab.arturbosch.detekt") version "1.23.1"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
    id("com.goncalossilva.resources") version "0.4.0"
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
    val nativeTarget = when {
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
            }
        }

        val ktSerializationVersion = "1.6.0"
        val ktorVersion = "2.3.5"
        val kotestVersion = "5.7.2"

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

            dependsOn(commonMain)

            val coroutinesVersion = "1.7.3"

            dependencies {
                implementation(kotlin("test"))
                implementation("io.ktor:ktor-client-logging:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
                implementation("io.kotest:kotest-assertions-core:$kotestVersion")
                implementation("io.kotest:kotest-framework-engine:$kotestVersion")
                implementation("com.goncalossilva:resources:0.4.0")
            }
        }

        val jvmMain by getting {
            dependsOn(commonMain)
        }
        val jvmTest by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("org.slf4j:slf4j-simple:2.0.9")
                implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
            }
        }

        val jsMain by getting {
            dependsOn(commonMain)
        }
        val jsTest by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:$ktorVersion")
            }
        }

        val nativeMain by getting {
            dependsOn(commonMain)
        }
        val nativeTest by getting {
            dependencies {
                if (isWindows) {
                    implementation("io.ktor:ktor-client-winhttp:$ktorVersion")
                } else if (isMacOs) {
                    implementation("io.ktor:ktor-client-darwin:$ktorVersion")
                } else {
                    implementation("io.ktor:ktor-client-cio:$ktorVersion")
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
