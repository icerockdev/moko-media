/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.icerock.moko.media.picker.MediaPickerController
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory

@Composable
actual fun rememberMediaPickerControllerFactory(): MediaPickerControllerFactory {
    val permissionsControllerFactory: PermissionsControllerFactory =
        rememberPermissionsControllerFactory()
    return remember(permissionsControllerFactory) {
        object : MediaPickerControllerFactory {
            override fun createMediaPickerController(
                permissionsController: PermissionsController
            ): MediaPickerController {
                return MediaPickerController(permissionsController)
            }

            override fun createMediaPickerController(): MediaPickerController {
                return MediaPickerController(
                    permissionsController = permissionsControllerFactory.createPermissionsController()
                )
            }
        }
    }
}
