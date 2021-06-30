/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("android-base-convention")
    id("detekt-convention")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform.android-manifest")
    id("dev.icerock.mobile.multiplatform.ios-framework")
}

kotlin {
    android()
    ios()
}

dependencies {
    commonMainApi(libs.coroutines)

    commonMainApi(libs.mokoPermissions)
    commonMainApi(libs.mokoMvvmCore)
    commonMainApi(libs.mokoMvvmLiveData)
    commonMainApi(projects.media)

    commonTestImplementation(libs.mokoTest)
    commonTestImplementation(libs.mokoMvvmTest)
    commonTestImplementation(libs.mokoPermissionsTest)
    commonTestImplementation(projects.mediaTest)
}

framework {
    export(projects.media)
    export(libs.mokoPermissions)
}
