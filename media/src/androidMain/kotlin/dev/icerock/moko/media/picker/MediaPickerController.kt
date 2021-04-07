/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import dev.icerock.moko.media.Bitmap
import dev.icerock.moko.media.FileMedia
import dev.icerock.moko.media.Media
import dev.icerock.moko.permissions.PermissionsController

actual interface MediaPickerController {
    actual val permissionsController: PermissionsController

    actual suspend fun pickImage(source: MediaSource): Bitmap
    actual suspend fun pickImage(source: MediaSource, maxWidth: Int, maxHeight: Int): Bitmap
    actual suspend fun pickMedia(): Media
    actual suspend fun pickFiles(): FileMedia

    fun bind(lifecycle: Lifecycle, fragmentManager: FragmentManager)

    companion object {
        operator fun invoke(
            permissionsController: PermissionsController,
            pickerFragmentTag: String = "MediaControllerPicker",
            filePickerFragmentTag: String = "FileMediaControllerPicker"
        ): MediaPickerController {
            return MediaPickerControllerImpl(
                permissionsController = permissionsController,
                pickerFragmentTag = pickerFragmentTag,
                filePickerFragmentTag = filePickerFragmentTag
            )
        }
    }
}
