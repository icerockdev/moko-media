/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    plugin(Deps.Plugins.androidLibrary)
    plugin(Deps.Plugins.kotlinMultiplatform)
    plugin(Deps.Plugins.mobileMultiplatform)
    plugin(Deps.Plugins.iosFramework)
}

dependencies {
    commonMainApi(Deps.Libs.MultiPlatform.coroutines)

    commonMainApi(Deps.Libs.MultiPlatform.mokoPermissions.common)
    commonMainApi(Deps.Libs.MultiPlatform.mokoMvvmCore)
    commonMainApi(Deps.Libs.MultiPlatform.mokoMvvmLiveData)
    commonMainApi(Deps.Libs.MultiPlatform.mokoMedia)
}

framework {
    export(project(":media"))
    export(Deps.Libs.MultiPlatform.mokoPermissions)
}
