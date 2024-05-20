/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.test

import androidx.activity.ComponentActivity
import dev.icerock.moko.media.Bitmap
import dev.icerock.moko.media.FileMedia
import dev.icerock.moko.media.Media
import dev.icerock.moko.media.picker.MediaPickerController
import dev.icerock.moko.media.picker.MediaSource
import dev.icerock.moko.permissions.PermissionsController

actual open class MediaPickerControllerMock actual constructor(
    actual override val permissionsController: PermissionsController
) : MediaPickerController {
    actual override suspend fun pickImage(source: MediaSource): Bitmap {
        TODO("Not yet implemented")
    }

    actual override suspend fun pickImage(
        source: MediaSource,
        maxWidth: Int,
        maxHeight: Int
    ): Bitmap {
        TODO("Not yet implemented")
    }

    actual override suspend fun pickMedia(): Media {
        TODO("Not yet implemented")
    }

    actual override suspend fun pickFiles(): FileMedia {
        TODO("Not yet implemented")
    }

    override fun bind(
        activity: ComponentActivity
    ) {
        TODO("Not yet implemented")
    }
}
