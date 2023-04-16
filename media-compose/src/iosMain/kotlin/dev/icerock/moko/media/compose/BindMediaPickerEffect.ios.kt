/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.compose

import androidx.compose.runtime.Composable
import dev.icerock.moko.media.picker.MediaPickerController
import dev.icerock.moko.permissions.compose.BindEffect

@Suppress("FunctionNaming")
@Composable
actual fun BindMediaPickerEffect(mediaPickerController: MediaPickerController) {
    BindEffect(mediaPickerController.permissionsController)
}
