plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":kollavarsham-core"))

    testImplementation(kotlin("test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
}
