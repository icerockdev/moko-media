/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media

expect class Bitmap {
    fun toByteArray(): ByteArray
    fun toBase64(): String
    fun toBase64WithCompress(maxSize: Int): String
}

private const val BASE64_IMAGE_MIME_PREFIX = "data:image/png;base64,"

fun Bitmap.toBase64WithCompressMIME(maxSize: Int) =
    "$BASE64_IMAGE_MIME_PREFIX${toBase64WithCompress(maxSize)}"

fun Bitmap.toBase64MIME() = "$BASE64_IMAGE_MIME_PREFIX${toBase64()}"
