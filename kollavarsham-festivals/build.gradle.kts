plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":kollavarsham-core"))
    api(project(":kollavarsham-panchangam"))

    testImplementation(kotlin("test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
}
