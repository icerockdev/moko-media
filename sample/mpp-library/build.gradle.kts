/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform")
    id("dev.icerock.mobile.multiplatform.ios-framework")
}

dependencies {
    commonMainApi(libs.coroutines)

    commonMainApi(libs.mokoPermissions)
    commonMainApi(libs.mokoMvvmCore)
    commonMainApi(libs.mokoMvvmLiveData)
    commonMainApi(libs.mokoMedia)

    commonTestImplementation(libs.mokoTest)
    commonTestImplementation(libs.mokoMvvmTest)
    commonTestImplementation(libs.mokoPermissionsTest)
    commonTestImplementation(projects.mediaTest)
}

framework {
    export(project(":media"))
    export("${libs.mokoPermissions.get().module.group}:${libs.mokoPermissions.get().module.name}:${libs.versions.mokoPermissionsVersion.get()}",
        "${libs.mokoPermissions.get().module.group}:${libs.mokoPermissions.get().module.name}:${libs.versions.mokoPermissionsVersion.get()}")
}
