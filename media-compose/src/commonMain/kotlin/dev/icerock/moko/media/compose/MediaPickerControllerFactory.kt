/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.compose

import androidx.compose.runtime.Composable
import dev.icerock.moko.media.picker.MediaPickerController
import dev.icerock.moko.permissions.PermissionsController

interface MediaPickerControllerFactory {
    fun createMediaPickerController(): MediaPickerController
    fun createMediaPickerController(permissionsController: PermissionsController): MediaPickerController
}

@Composable
expect fun rememberMediaPickerControllerFactory(): MediaPickerControllerFactory
