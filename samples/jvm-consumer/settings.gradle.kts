// A standalone consumer of the published SDK artifacts. It is deliberately NOT part of the SDK
// build: it resolves the library from Maven local, exactly as an external app would.
// Run `./gradlew publishToMavenLocal` in the repository root first.
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

rootProject.name = "jvm-consumer"
