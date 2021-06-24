/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform")
    id("org.gradle.maven-publish")
}

kotlin {
    sourceSets {
        val iosArm64Main by getting
        val iosX64Main by getting

        iosArm64Main.dependsOn(iosX64Main)
    }
}

dependencies {
    commonMainImplementation(libs.coroutines)
    commonMainApi(libs.mokoPermissions)

    androidMainImplementation(libs.appCompat)
    androidMainImplementation(libs.exifInterface)

    // TODO #34 remove external dependency
    androidMainImplementation(libs.mediaFilePicker)
}
