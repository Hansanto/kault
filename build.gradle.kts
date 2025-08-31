import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    libs.plugins.run {
        alias(kt.multiplatform)
        alias(kt.serialization)
        alias(ksp)
        alias(kotest)
        alias(kover)
        alias(dokka)
        alias(detekt)
        alias(ktlint)
        alias(resources)
        alias(gradle.publish)
    }
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

val reportFolder = file("reports")

detekt {
    ignoreFailures = System.getenv("DETEKT_IGNORE_FAILURES")?.toBooleanStrictOrNull() ?: false
    config.from(file("config/detekt/detekt.yml"))
    reportsDir = reportFolder.resolve("detekt")
}

kover {
    reports {
        val reportKoverFolder = reportFolder.resolve("kover")

        total {
            xml {
                this.xmlFile.set(reportKoverFolder.resolve("xml/result.xml"))
            }
            html {
                this.htmlDir.set(reportKoverFolder.resolve("html"))
            }
        }

        filters {
            excludes {
                packages("*.payload", "*.response", "*.common")
                classes("*Exception")
            }
        }
    }
}

ktlint {
    reporters {
        reporter(ReporterType.HTML)
        reporter(ReporterType.CHECKSTYLE)
    }
}

val jvmTargetVersion = JvmTarget.JVM_11
val jvmTargetVersionNumber = jvmTargetVersion.target.toInt()

kotlin {
    applyDefaultHierarchyTemplate()
    explicitApi = org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Strict
    jvmToolchain(jvmTargetVersionNumber)

    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget = jvmTargetVersion
        }

        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }

    fun KotlinJsTargetDsl.jsAndWasmSharedConfigurationTarget() {
        nodejs()
        binaries.library()
        useCommonJs()
        generateTypeScriptDefinitions()
    }
    js {
        jsAndWasmSharedConfigurationTarget()
    }
    /**
     * https://youtrack.jetbrains.com/issue/KT-70075
     * Not supported by:
     * - resources
     */
    // wasmJs {
    //   jsAndWasmSharedConfigurationTarget()
    // }
    /**
     * Not supported by:
     * - ktor
     * - kotlinx-datetime
     * - kotest
     * - resources
     */
    // wasmWasi()

    // Native tiers: https://kotlinlang.org/docs/native-target-support.html
    // Tier 1
    macosX64()
    macosArm64()
    iosSimulatorArm64()
    iosX64()

    // Tier 2
    linuxX64()
    linuxArm64()
    watchosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()
    iosArm64()

    // Tier 3
    mingwX64()
    /**
     * Not supported by:
     * - ktor
     * - kotest
     * - resources
     */
    // watchosDeviceArm64()

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.contracts.ExperimentalContracts")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlin.ExperimentalStdlibApi")
                optIn("kotlin.time.ExperimentalTime")
            }
        }

        commonMain.dependencies {
            api(ktorLibs.client.core)
            api(ktorLibs.serialization)
            api(ktorLibs.serialization.kotlinx.json)
            api(ktorLibs.client.contentNegotiation)
            api(libs.bundles.kt.common)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.bundles.kotest.common)
            implementation(ktorLibs.client.logging)
            implementation(libs.kt.io)
            implementation(libs.resources)
        }

        jvmTest.dependencies {
            implementation(ktorLibs.client.cio)
            implementation(libs.slf4j.simple)
            implementation(libs.kotest.junit5)
        }

        jsTest.dependencies {
            implementation(ktorLibs.client.js)
        }

        appleTest.dependencies {
            implementation(ktorLibs.client.darwin)
        }

        linuxTest.dependencies {
            implementation(ktorLibs.client.cio)
        }

        mingwTest.dependencies {
            implementation(ktorLibs.client.winhttp)
        }
    }
}

val javadocJar = tasks.register<Jar>("dokkaJavadocJar") {
    group = "dokka"
    description = "Creates a jar containing the documentation."
    archiveClassifier.set("javadoc")
    dependsOn(tasks.dokkaGenerate)
}

tasks {
    withType<org.jlleitschuh.gradle.ktlint.tasks.GenerateReportsTask> {
        reportsOutputDirectory.set(reportFolder.resolve("klint/$name"))
    }

    withType<Detekt>().configureEach {
        jvmTarget = jvmTargetVersion.target

        exclude {
            it.file.path.contains("/build/")
        }

        reports {
            html.required.set(true)
            xml.required.set(true)
            txt.required.set(false)
            sarif.required.set(false)
            md.required.set(false)
        }
    }

    withType<DetektCreateBaselineTask>().configureEach {
        jvmTarget = jvmTargetVersion.target
    }

    register("detektAll") {
        group = JavaBasePlugin.VERIFICATION_GROUP
        allprojects {
            this@register.dependsOn(tasks.withType<Detekt>())
        }
    }
}

deployer {
    content {
        kotlinComponents {
           docs(javadocJar)
        }
    }

    localSpec()
    // https://opensource.deepmedia.io/deployer
    centralPortalSpec {
        // Generate key pair from https://central.sonatype.com/account
        auth.user.set(secret("REPOSITORY_USERNAME"))
        auth.password.set(secret("REPOSITORY_PASSWORD"))

        signing.key.set(secret("SIGNING_KEY"))
        signing.password.set(secret("SIGNING_PASSWORD"))
    }

    projectInfo {
        // https://opensource.deepmedia.io/deployer/configuration
        val projectName = project.name
        val projectOrganizationPath = "Hansanto/$projectName"
        val projectGitUrl = "https://github.com/$projectOrganizationPath"

        url.set(projectGitUrl)

        scm {
            fromGithub("Hansanto", projectName)
        }

        license(apache2)
        developer {
            name.set("Hansanto")
            url.set("https://github.com/Hansanto")
            email.set("anthony.hanson@outlook.fr")
        }
    }
}
