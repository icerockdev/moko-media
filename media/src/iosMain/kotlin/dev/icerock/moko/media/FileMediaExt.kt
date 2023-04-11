/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL

fun FileMedia.toNSData(): NSData {
    val url = NSURL.URLWithString(this.path)
        ?: throw IllegalArgumentException("invalid file path")
    return NSData.dataWithContentsOfURL(url) ?: throw IllegalArgumentException("invalid file data")
}

actual fun FileMedia.toByteArray(): ByteArray {
    val data = toNSData()
    val bytes = data.bytes ?: throw IllegalArgumentException("file bytes is null")
    val length = data.length

    val bytesPointer: CPointer<ByteVar> = bytes.reinterpret()
    return ByteArray(length.toInt()) { index -> bytesPointer[index] }
}
