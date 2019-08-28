/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import java.net.URI

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("kotlin-android-extensions")
    id("dev.icerock.mobile.multiplatform")
    id("maven-publish")
}

group = "dev.icerock.moko"
version = "0.1.0"

android {
    compileSdkVersion(Versions.Android.compileSdk)

    defaultConfig {
        minSdkVersion(Versions.Android.minSdk)
        targetSdkVersion(Versions.Android.targetSdk)
    }
}

androidExtensions {
    isExperimental = true
}

dependencies {
    mppLibrary(Deps.Libs.MultiPlatform.kotlinStdLib)

    mppLibrary(Deps.Libs.MultiPlatform.mokoPermissions)

    androidLibrary(Deps.Libs.Android.appCompat)
    androidLibrary(Deps.Libs.Android.exifInterface)

    // TODO remove external dependency
    androidLibrary(Deps.Libs.Android.mediaFilePicker)
}

publishing {
    repositories.maven("https://api.bintray.com/maven/icerockdev/moko/moko-media/;publish=1") {
        name = "bintray"

        credentials {
            username = System.getProperty("BINTRAY_USER")
            password = System.getProperty("BINTRAY_KEY")
        }
    }
}
