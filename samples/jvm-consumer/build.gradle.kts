plugins {
    kotlin("jvm") version "2.0.21"
    application
}

dependencies {
    // Only the public artifact; kollavarsham-core comes transitively.
    implementation("io.github.anjalymsajeev:kollavarsham-locale:0.1.0-alpha01")
}

kotlin { jvmToolchain(21) }

application {
    mainClass.set("sample.MainKt")
    // Print Malayalam correctly even when the terminal locale is not UTF-8.
    applicationDefaultJvmArgs = listOf("-Dstdout.encoding=UTF-8")
}

// The same SDK called from Java: ../../gradlew runJava
tasks.register<JavaExec>("runJava") {
    group = "application"
    description = "Runs the Java consumer example."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("sample.JavaMain")
    jvmArgs("-Dstdout.encoding=UTF-8")
}
