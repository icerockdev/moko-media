plugins {
    id("dev.icerock.moko.gradle.android.application")
    id("dev.icerock.moko.gradle.detekt")
    id("org.jetbrains.compose")
}

android {
    namespace = "com.icerockdev"
    buildFeatures.compose = true

    defaultConfig {
        applicationId = "dev.icerock.moko.samples.media"

        minSdk = 21
        compileSdk = 34
        targetSdk = 34

        versionCode = 1
        versionName = "0.1.0"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {
    implementation(libs.appCompat)
    implementation(libs.androidxCore)
    implementation(libs.composeActivity)
    implementation(libs.composeMaterial)
    implementation(projects.mediaCompose)
    implementation(projects.sample.mppLibrary)
}
