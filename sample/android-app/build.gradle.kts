plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdkVersion(libs.versions.compileSdk.get().toInt())

    buildFeatures.viewBinding = true

    dexOptions {
        javaMaxHeapSize = "2g"
    }

    defaultConfig {
        minSdkVersion(libs.versions.minSdk.get())
        targetSdkVersion(libs.versions.targetSdk.get())

        applicationId = "dev.icerock.moko.samples.media"

        versionCode = 1
        versionName = "0.1.0"

        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    packagingOptions {
        exclude("META-INF/*.kotlin_module")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appCompat)

    implementation(projects.sample.mppLibrary)
}
