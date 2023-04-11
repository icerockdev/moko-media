/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.icerock.moko.media.picker.ios.MediaPickerController
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

@Composable
actual fun rememberMediaPickerControllerFactory(): MediaPickerControllerFactory {
    val permissionsControllerFactory: PermissionsControllerFactory =
        rememberPermissionsControllerFactory()
    return remember(permissionsControllerFactory) {
        val getVC: () -> UIViewController = {
            // for compose app in common case used one ComposeViewController in one window. so it will works
            UIApplication.sharedApplication.keyWindow?.rootViewController
                ?: error("can't find view controller")
        }

        object : MediaPickerControllerFactory {
            override fun createMediaPickerController(
                permissionsController: PermissionsController
            ): MediaPickerController {
                return MediaPickerController(
                    permissionsController = permissionsController,
                    getViewController = getVC
                )
            }

            override fun createMediaPickerController(): MediaPickerController {
                return MediaPickerController(
                    permissionsController = permissionsControllerFactory.createPermissionsController(),
                    getViewController = getVC
                )
            }
        }
    }
}
