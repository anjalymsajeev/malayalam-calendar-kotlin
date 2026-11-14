plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "io.github.anjalymsajeev.kollavarsham.sample"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.anjalymsajeev.kollavarsham.sample"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = property("VERSION_NAME") as String
    }

    compileOptions {
        // Demonstrates the documented requirement: java.time below API 26 needs desugaring.
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            // Signed with the debug key so the sample can be installed for review; not for the Play Store.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    buildFeatures { compose = true }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

ktlint {
    version.set(libs.versions.ktlint.get())
}

dependencies {
    implementation(project(":kollavarsham-android"))
    implementation(project(":kollavarsham-panchangam"))
    implementation(project(":kollavarsham-festivals"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    testImplementation(libs.junit4)
    testImplementation(kotlin("test"))
}
