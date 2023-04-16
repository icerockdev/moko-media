/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.compose

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import dev.icerock.moko.media.Bitmap
import org.jetbrains.skia.Image

actual fun Bitmap.toImageBitmap(): ImageBitmap {
    return Image.makeFromEncoded(this.toByteArray()).toComposeImageBitmap()
}
