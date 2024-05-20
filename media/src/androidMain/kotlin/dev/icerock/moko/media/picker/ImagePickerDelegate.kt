/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import android.content.Context
import android.net.Uri
import dev.icerock.moko.media.BitmapUtils

internal abstract class ImagePickerDelegate<C : PickerDelegate.CallbackData<D>, I, D> : PickerDelegate<C, I, D>() {

    @Suppress("ReturnCount")
    protected fun processResult(
        context: Context,
        callback: (Result<android.graphics.Bitmap>) -> Unit,
        uri: Uri,
        maxImageWidth: Int = DEFAULT_MAX_IMAGE_WIDTH,
        maxImageHeight: Int = DEFAULT_MAX_IMAGE_HEIGHT,
    ) {
        val contentResolver = context.contentResolver

        val bitmapOptions = contentResolver.openInputStream(uri)?.use {
            BitmapUtils.getBitmapOptionsFromStream(it)
        } ?: run {
            callback.invoke(Result.failure(NoAccessToFileException(uri.toString())))
            return
        }

        val sampleSize =
            BitmapUtils.calculateInSampleSize(bitmapOptions, maxImageWidth, maxImageHeight)

        val orientation = contentResolver.openInputStream(uri)?.use {
            BitmapUtils.getBitmapOrientation(it)
        } ?: run {
            callback.invoke(Result.failure(NoAccessToFileException(uri.toString())))
            return
        }

        val bitmap = contentResolver.openInputStream(uri)?.use {
            BitmapUtils.getNormalizedBitmap(it, orientation, sampleSize)
        } ?: run {
            callback.invoke(Result.failure(NoAccessToFileException(uri.toString())))
            return
        }

        callback.invoke(Result.success(bitmap))
    }
}
