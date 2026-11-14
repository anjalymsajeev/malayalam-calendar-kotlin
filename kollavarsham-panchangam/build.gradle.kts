plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    compilerOptions {
        optIn.add("io.github.anjalymsajeev.kollavarsham.astronomy.KollavarshamAstronomyApi")
    }
}

dependencies {
    api(project(":kollavarsham-core"))
    api(project(":kollavarsham-locale"))
    implementation(project(":kollavarsham-astronomy"))

    testImplementation(kotlin("test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
}
