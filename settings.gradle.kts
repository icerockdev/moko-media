/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()

        jcenter {
            content {
                includeGroup("org.jetbrains.kotlinx")
            }
        }

        maven { url = uri("https://jitpack.io") }
    }
}

includeBuild("media-build-logic")

include(":media")
include(":media-test")
include(":sample:android-app")
include(":sample:mpp-library")
