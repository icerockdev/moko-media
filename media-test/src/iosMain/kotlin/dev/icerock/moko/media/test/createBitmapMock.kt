/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.test

import dev.icerock.moko.media.Bitmap
import platform.CoreGraphics.CGRectMake
import platform.CoreImage.CIImage
import platform.UIKit.UIImage

actual fun createBitmapMock(): Bitmap {
    val image = CIImage.clearImage.imageByCroppingToRect(CGRectMake(0.0, 0.0, 1.0, 1.0))
    return Bitmap(UIImage(image))
}
