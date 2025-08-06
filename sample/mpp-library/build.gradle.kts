/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("dev.icerock.moko.gradle.multiplatform.mobile")
    id("dev.icerock.mobile.multiplatform.ios-framework")
    id("dev.icerock.moko.gradle.detekt")
}

android {
    namespace = "com.icerockdev.library"
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
