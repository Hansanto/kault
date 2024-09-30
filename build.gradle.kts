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

val signingKey: String? = System.getenv("SIGNING_KEY")
val signingPassword: String? = System.getenv("SIGNING_PASSWORD")
if (signingKey != null && signingPassword != null) {
    signing {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("REPOSITORY_USERNAME"))
            password.set(System.getenv("REPOSITORY_PASSWORD"))
        }
    }
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
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

val jvmTargetVersion = JvmTarget.JVM_1_8
val jvmTargetVersionNumber = 8

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
    js(IR) {
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
            }
        }

        commonMain.dependencies {
            api(libs.bundles.ktor.common)
            api(libs.bundles.kt.common)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.bundles.kotest.common)
            implementation(libs.ktor.logging)
            implementation(libs.kt.io)
            implementation(libs.resources)
        }

        jvmTest.dependencies {
            implementation(libs.ktor.cio)
            implementation(libs.slf4j.simple)
            implementation(libs.kotest.junit5)
        }

        jsTest.dependencies {
            implementation(libs.ktor.js)
        }

        appleTest.dependencies {
            implementation(libs.ktor.darwin)
        }

        linuxTest.dependencies {
            implementation(libs.ktor.cio)
        }

        mingwTest.dependencies {
            implementation(libs.ktor.winhttp)
        }
    }
}

val dokkaOutputDir = file("dokka")

val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Deletes the dokka output directory."
    delete(dokkaOutputDir)
}

val javadocJar = tasks.register<Jar>("docJar") {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Creates a jar containing the documentation."
    dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaOutputDir)
}

//region Fix Gradle warning about signing tasks using publishing task outputs without explicit dependencies
// https://github.com/gradle/gradle/issues/26091
tasks.withType<AbstractPublishToMaven>().configureEach {
    val signingTasks = tasks.withType<Sign>()
    mustRunAfter(signingTasks)
}
//endregion

tasks {
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        reporters {
            reporter(ReporterType.HTML)
            reporter(ReporterType.CHECKSTYLE)
        }
    }

    withType<org.jlleitschuh.gradle.ktlint.tasks.GenerateReportsTask> {
        reportsOutputDirectory.set(reportFolder.resolve("klint/$name"))
    }

    withType<Detekt>().configureEach {
        jvmTarget = jvmTargetVersion.target

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

    clean {
        delete(dokkaOutputDir)
    }

    dokkaHtml.configure {
        dependsOn(deleteDokkaOutputDir)
        outputDirectory.set(file(dokkaOutputDir))
    }
}

publishing {
    publications {
        val projectName = project.name
        val projectOrganizationPath = "Hansanto/$projectName"
        val projectGitUrl = "https://github.com/$projectOrganizationPath"

        withType<MavenPublication> {
            artifact(javadocJar)
            pom {
                name.set(rootProject.name)
                description.set(project.description)
                url.set(projectGitUrl)

                issueManagement {
                    system.set("GitHub")
                    url.set("$projectGitUrl/issues")
                }

                ciManagement {
                    system.set("GitHub Actions")
                }

                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/")
                    }
                }

                developers {
                    developer {
                        name.set("Hansanto")
                        email.set("anthony.hanson@outlook.fr")
                        url.set("https://github.com/Hansanto")
                    }
                }

                scm {
                    connection.set("scm:git:$projectGitUrl.git")
                    developerConnection.set("scm:git:git@github.com:$projectOrganizationPath.git")
                    url.set(projectGitUrl)
                }

                distributionManagement {
                    downloadUrl.set("$projectGitUrl/releases")
                }
            }
        }
    }
}
