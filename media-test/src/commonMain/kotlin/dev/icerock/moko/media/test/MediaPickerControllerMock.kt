/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.test

import dev.icerock.moko.media.Bitmap
import dev.icerock.moko.media.FileMedia
import dev.icerock.moko.media.Media
import dev.icerock.moko.media.picker.MediaPickerController
import dev.icerock.moko.media.picker.MediaSource
import dev.icerock.moko.permissions.PermissionsController

expect open class MediaPickerControllerMock constructor(
    permissionsController: PermissionsController
) : MediaPickerController {
    override val permissionsController: PermissionsController

    override suspend fun pickImage(source: MediaSource): Bitmap
    override suspend fun pickImage(
        source: MediaSource,
        maxWidth: Int,
        maxHeight: Int
    ): Bitmap

    override suspend fun pickMedia(): Media
    override suspend fun pickFiles(): FileMedia
}

fun createMediaPickerControllerMock(
    permissionsController: PermissionsController,
    galleryImageResult: Bitmap,
    cameraImageResult: Bitmap
): MediaPickerControllerMock = object : MediaPickerControllerMock(permissionsController) {
    override suspend fun pickImage(source: MediaSource): Bitmap {
        return source.result()
    }

    override suspend fun pickImage(source: MediaSource, maxWidth: Int, maxHeight: Int): Bitmap {
        return source.result()
    }

    private fun MediaSource.result() = when (this) {
        MediaSource.GALLERY -> galleryImageResult
        MediaSource.CAMERA -> cameraImageResult
    }
}

fun createMediaPickerControllerMock(
    permissionsController: PermissionsController,
    mediaResult: Media
): MediaPickerControllerMock = object : MediaPickerControllerMock(permissionsController) {
    override suspend fun pickMedia(): Media {
        return mediaResult
    }
}

fun createMediaPickerControllerMock(
    permissionsController: PermissionsController,
    fileMediaResult: FileMedia
): MediaPickerControllerMock = object : MediaPickerControllerMock(permissionsController) {
    override suspend fun pickFiles(): FileMedia {
        return fileMediaResult
    }
}
