/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media

import android.util.Base64
import java.io.ByteArrayOutputStream

actual class Bitmap(val platformBitmap: android.graphics.Bitmap) {
    actual fun toByteArray(): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        platformBitmap.compress(
            android.graphics.Bitmap.CompressFormat.PNG,
            100,
            byteArrayOutputStream
        )
        return byteArrayOutputStream.toByteArray()
    }

    actual fun toBase64(): String {
        val byteArray = toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    actual fun toBase64WithCompress(maxSize: Int): String {
        val compressedBitmap = BitmapUtils.getResizedBitmap(platformBitmap, maxSize)
        val bitmap = Bitmap(compressedBitmap)
        return bitmap.toBase64()
    }
}
