import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.bcv)
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.dokka)
}

apiValidation {
    // Nothing excluded: every library module's public API is tracked.
}

subprojects {
    group = property("GROUP") as String
    version = property("VERSION_NAME") as String

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        apply(plugin = "org.jlleitschuh.gradle.ktlint")
        apply(plugin = "org.jetbrains.dokka")
        apply(plugin = "maven-publish")

        extensions.configure<KotlinJvmProjectExtension> {
            explicitApi()
            jvmToolchain(21)
            compilerOptions {
                // JVM 11 bytecode keeps the libraries consumable by Android (D8 desugars as needed).
                jvmTarget.set(JvmTarget.JVM_11)
                allWarningsAsErrors.set(true)
            }
        }
        extensions.configure<JavaPluginExtension> { withSourcesJar() }
        tasks.withType<JavaCompile>().configureEach { options.release.set(11) }
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
            testLogging {
                events("failed")
                exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            }
        }

        extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
            version.set(rootProject.libs.versions.ktlint.get())
            filter { exclude { it.file.name == "Vsop87Earth.kt" } } // generated
        }

        tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
            dokkaSourceSets.configureEach {
                // Keep doc generation offline-friendly: no fetching of external package lists.
                noJdkLink.set(true)
                noStdlibLink.set(true)
                noAndroidSdkLink.set(true)
                includes.from(project.file("Module.md").takeIf { it.exists() } ?: emptyList<File>())
            }
        }

        extensions.configure<PublishingExtension> {
            publications.create<MavenPublication>("maven") {
                from(components["java"])
                pom {
                    name.set(project.name)
                    description.set("Kotlin SDK for the Malayalam (Kollavarsham) calendar: ${project.name}")
                    url.set("https://github.com/anjalymsajeev/malayalam-calendar-kotlin")
                    licenses {
                        license {
                            name.set("Apache License 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }
                    developers {
                        developer {
                            id.set("anjalymsajeev")
                            name.set("anjalymsajeev")
                            url.set("https://github.com/anjalymsajeev")
                        }
                    }
                    scm {
                        url.set("https://github.com/anjalymsajeev/malayalam-calendar-kotlin")
                        connection.set("scm:git:https://github.com/anjalymsajeev/malayalam-calendar-kotlin.git")
                    }
                }
            }
        }
    }
}
