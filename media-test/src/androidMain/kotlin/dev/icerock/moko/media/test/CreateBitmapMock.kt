/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.test

import dev.icerock.moko.media.Bitmap

actual fun createBitmapMock(): Bitmap {
    return Bitmap(object : Bitmap.Delegate {
        override fun getAndroidBitmap(): android.graphics.Bitmap {
            TODO("Not yet implemented")
        }

        override fun getByteArray(): ByteArray {
            return ByteArray(0)
        }

        override fun getResized(maxSize: Int): Bitmap {
            return createBitmapMock()
        }
    })
}
