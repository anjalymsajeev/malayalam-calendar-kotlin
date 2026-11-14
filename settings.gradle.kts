pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "kollavarsham-kotlin"

include(
    ":kollavarsham-astronomy",
    ":kollavarsham-core",
    ":kollavarsham-locale",
    ":kollavarsham-panchangam",
    ":kollavarsham-festivals",
    ":kollavarsham-android",
    ":sample-app",
)
