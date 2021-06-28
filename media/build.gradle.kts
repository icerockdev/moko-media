/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("multiplatform-library-convention")
    id("dev.icerock.mobile.multiplatform.android-manifest")
    id("android-publication-convention")
}

dependencies {
    commonMainImplementation(libs.coroutines)
    commonMainApi(libs.mokoPermissions)

    "androidMainImplementation"(libs.appCompat)
    "androidMainImplementation"(libs.exifInterface)

    // TODO #34 remove external dependency
    "androidMainImplementation"(libs.mediaFilePicker)
}
