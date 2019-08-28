/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import dev.icerock.moko.media.Bitmap
import dev.icerock.moko.media.FileMedia
import dev.icerock.moko.media.Media

expect class MediaPickerController {
    suspend fun pickImage(source: MediaSource): Bitmap
    suspend fun pickMedia(): Media
    suspend fun pickFiles(): FileMedia
}
