plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.icerock.mobile.multiplatform")
}

android {
    compileSdkVersion(Versions.Android.compileSdk)

    defaultConfig {
        minSdkVersion(Versions.Android.minSdk)
        targetSdkVersion(Versions.Android.targetSdk)
    }
}

val libs = listOf(
    Deps.Libs.MultiPlatform.mokoMedia,
    Deps.Libs.MultiPlatform.mokoPermissions
)

setupFramework(
    exports = libs
)

dependencies {
    mppLibrary(Deps.Libs.MultiPlatform.kotlinStdLib)

    libs.forEach { mppLibrary(it) }
}
