plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    compilerOptions {
        optIn.add("io.github.anjalymsajeev.kollavarsham.astronomy.KollavarshamAstronomyApi")
    }
}

dependencies {
    api(project(":kollavarsham-astronomy"))

    testImplementation(kotlin("test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
}

// Guard for the architecture rule "core must not depend on Android or UI frameworks".
val verifyNoAndroidDependencies by tasks.registering {
    val classpath = configurations.named("runtimeClasspath")
    doLast {
        val forbidden = listOf("com.android", "androidx", "com.google.android", "org.jetbrains.compose")
        val offenders =
            classpath
                .get()
                .resolvedConfiguration.resolvedArtifacts
                .map { it.moduleVersion.id.group }
                .filter { group -> forbidden.any { group.startsWith(it) } }
        check(offenders.isEmpty()) { "kollavarsham-core must not depend on Android/UI libraries: $offenders" }
    }
}
tasks.named("check") { dependsOn(verifyNoAndroidDependencies) }
