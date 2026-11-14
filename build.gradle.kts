import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.bcv)
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

apiValidation {
    // Every library module's public API is tracked; the sample app is not a library.
    ignoredProjects += "sample-app"
}

subprojects {
    group = property("GROUP") as String
    version = property("VERSION_NAME") as String

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        apply(plugin = "org.jlleitschuh.gradle.ktlint")
        apply(plugin = "org.jetbrains.dokka")
        apply(plugin = "com.vanniktech.maven.publish")

        extensions.configure<KotlinJvmProjectExtension> {
            explicitApi()
            jvmToolchain(21)
            compilerOptions {
                // JVM 11 bytecode keeps the libraries consumable by Android (D8 desugars as needed).
                jvmTarget.set(JvmTarget.JVM_11)
                allWarningsAsErrors.set(true)
            }
        }
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
            filter { exclude { it.file.name == "Vsop87Earth.kt" || it.file.name == "MoonTerms.kt" } } // generated
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
        extensions.configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
            configure(
                com.vanniktech.maven.publish.KotlinJvm(
                    javadocJar = com.vanniktech.maven.publish.JavadocJar.Dokka("dokkaHtml"),
                    sourcesJar = true,
                ),
            )
        }
    }

    // Shared Maven Central settings for every published module (JVM and Android).
    pluginManager.withPlugin("com.vanniktech.maven.publish") {
        // Maven Central (Central Portal). Uploads are signed when a key is supplied (CI release
        // workflow); local publishToMavenLocal builds work unsigned. See RELEASING.md.
        extensions.configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
            // automaticRelease = false: each upload waits in the Central Portal for a human to
            // inspect it and press Publish. Releases to Maven Central cannot be deleted.
            publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL, automaticRelease = false)
            if (providers.gradleProperty("signingInMemoryKey").isPresent) signAllPublications()
            pom {
                name.set(project.name)
                description.set("Kotlin SDK for the Malayalam (Kollavarsham) calendar: ${project.name}")
                inceptionYear.set("2026")
                url.set("https://github.com/anjalymsajeev/malayalam-calendar-kotlin")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        distribution.set("repo")
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
                    developerConnection.set("scm:git:ssh://git@github.com/anjalymsajeev/malayalam-calendar-kotlin.git")
                }
            }
        }
    }
}
