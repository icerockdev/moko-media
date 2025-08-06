plugins {
    id("dev.icerock.moko.gradle.android.application")
    id("dev.icerock.moko.gradle.detekt")
}

android {
    namespace = "com.icerockdev"
    buildFeatures.viewBinding = true

    defaultConfig {
        applicationId = "dev.icerock.moko.samples.media"

        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation(libs.appCompat)

    implementation(projects.sample.mppLibrary)
}
