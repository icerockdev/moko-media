/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media

import java.io.File

actual fun FileMedia.toByteArray(): ByteArray {
    val file = File(path)
    return file.readBytes()
}
