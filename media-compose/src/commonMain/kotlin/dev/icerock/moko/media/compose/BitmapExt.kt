/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.compose

import androidx.compose.ui.graphics.ImageBitmap
import dev.icerock.moko.media.Bitmap

expect fun Bitmap.toImageBitmap(): ImageBitmap
