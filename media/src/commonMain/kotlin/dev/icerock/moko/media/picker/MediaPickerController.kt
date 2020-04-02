/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import dev.icerock.moko.media.Bitmap
import dev.icerock.moko.media.FileMedia
import dev.icerock.moko.media.Media

internal const val DEFAULT_MAX_IMAGE_WIDTH = 1024
internal const val DEFAULT_MAX_IMAGE_HEIGHT = 1024

expect class MediaPickerController {
    suspend fun pickImage(source: MediaSource): Bitmap
    suspend fun pickImage(source: MediaSource, maxWidth: Int, maxHeight: Int): Bitmap
    suspend fun pickMedia(): Media
    suspend fun pickFiles(): FileMedia
}
