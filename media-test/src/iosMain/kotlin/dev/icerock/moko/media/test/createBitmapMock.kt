/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.test

import dev.icerock.moko.media.Bitmap
import platform.UIKit.UIImage

actual fun createBitmapMock(): Bitmap {
    return Bitmap(UIImage())
}
