import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
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

kotlin {
    explicitApi = org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Strict
    jvmToolchain(8)

    iosX64()
    iosArm64()
    iosSimulatorArm64()
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()
    watchosArm32()
    watchosArm64()
    /**
     * Not supported yet by:
     * - ktor
     * - kotest
     */
    // watchosDeviceArm64()
    watchosX64()
    watchosSimulatorArm64()
    linuxX64()
    linuxArm64()
    macosX64()
    macosArm64()
    mingwX64()
    jvm {
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
    wasmJs {
        jsAndWasmSharedConfigurationTarget()
    }
    /**
     * Not supported yet by:
     * - ktor
     * - kotlinx-datetime
     * - kotest
     */
    // wasmWasi()

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.contracts.ExperimentalContracts")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlin.ExperimentalStdlibApi")
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
                implementation(libs.kt.io)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.ktor.cio)
                implementation(libs.slf4j.simple)
                implementation(libs.kotest.junit5)
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(libs.ktor.js)
            }
        }

        val mingwX64Test by getting {
            dependencies {
                implementation(libs.ktor.winhttp)
            }
        }
        val linuxArm64Test by getting {
            dependencies {
                implementation(libs.ktor.cio)
            }
        }
        val linuxX64Test by getting {
            dependencies {
                implementation(libs.ktor.cio)
            }
        }
        val macosArm64Test by getting {
            dependencies {
                implementation(libs.ktor.darwin)
            }
        }
        val macosX64Test by getting {
            dependencies {
                implementation(libs.ktor.darwin)
            }
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
