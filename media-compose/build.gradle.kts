/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("dev.icerock.moko.gradle.multiplatform.mobile")
    id("dev.icerock.moko.gradle.publication")
    id("dev.icerock.moko.gradle.stub.javadoc")
    id("dev.icerock.moko.gradle.detekt")
    id("org.jetbrains.compose")
}

android {
    namespace = "dev.icerock.moko.media.compose"

    defaultConfig {
        minSdk = 21
    }
}

dependencies {
    commonMainApi(projects.media)
    commonMainApi(compose.runtime)
    commonMainApi(compose.ui)
    commonMainApi(libs.mokoPermissionsCompose)

    androidMainImplementation(libs.appCompat)

    // without this i got Could not find "moko-media/media-compose/build/kotlinTransformedMetadataLibraries/commonMain/org.jetbrains.kotlinx-atomicfu-0.17.3-nativeInterop-8G5yng.klib"
    commonMainImplementation("org.jetbrains.kotlinx:atomicfu:0.17.3")
}
