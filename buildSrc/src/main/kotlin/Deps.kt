/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

object Deps {
    private const val kotlinVersion = "1.4.0"

    private const val androidAppCompatVersion = "1.1.0"
    private const val materialDesignVersion = "1.0.0"
    private const val androidLifecycleVersion = "2.1.0"
    private const val androidCoreTestingVersion = "2.1.0"
    private const val androidExifInterface = "1.0.0"
    private const val androidMediaFilePicker = "1.8"

    private const val coroutinesVersion = "1.3.9"
    private const val mokoMvvmVersion = "0.8.0"
    private const val mokoPermissionsVersion = "0.6.0"
    const val mokoMediaVersion = "0.6.0"

    object Android {
        const val compileSdk = 28
        const val targetSdk = 28
        const val minSdk = 16
    }

    object Plugins {
        val androidApplication = GradlePlugin(id = "com.android.application")
        val androidLibrary = GradlePlugin(id = "com.android.library")
        val kotlinMultiplatform = GradlePlugin(id = "org.jetbrains.kotlin.multiplatform")
        val kotlinAndroid = GradlePlugin(id = "kotlin-android")
        val mavenPublish = GradlePlugin(id = "org.gradle.maven-publish")

        val mobileMultiplatform = GradlePlugin(id = "dev.icerock.mobile.multiplatform")
        val iosFramework = GradlePlugin(id = "dev.icerock.mobile.multiplatform.ios-framework")
    }

    object Libs {
        object Android {
            const val appCompat = "androidx.appcompat:appcompat:$androidAppCompatVersion"
            const val material = "com.google.android.material:material:$materialDesignVersion"
            const val lifecycle = "androidx.lifecycle:lifecycle-extensions:$androidLifecycleVersion"
            const val exifInterface = "androidx.exifinterface:exifinterface:$androidExifInterface"
            const val mediaFilePicker = "com.nbsp:library:$androidMediaFilePicker"
        }

        object MultiPlatform {
            const val coroutines =
                "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion"
            val mokoPermissions = MultiPlatformLibrary(
                common = "dev.icerock.moko:permissions:$mokoPermissionsVersion",
                iosX64 = "dev.icerock.moko:permissions-iosx64:$mokoPermissionsVersion",
                iosArm64 = "dev.icerock.moko:permissions-iosarm64:$mokoPermissionsVersion"
            )
            const val mokoMvvm = "dev.icerock.moko:mvvm:$mokoMvvmVersion"
            const val mokoMedia = "dev.icerock.moko:media:$mokoMediaVersion"
        }

        object Tests {
            const val kotlinTestJUnit =
                "org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion"
            const val androidCoreTesting =
                "androidx.arch.core:core-testing:$androidCoreTestingVersion"
        }
    }
}
