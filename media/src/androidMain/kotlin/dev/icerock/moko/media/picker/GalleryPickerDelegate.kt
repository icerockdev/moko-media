/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import android.content.Context
import android.graphics.Bitmap
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

internal class GalleryPickerDelegate :
    ImagePickerDelegate<GalleryPickerDelegate.GalleryPickerCallbackData, PickVisualMediaRequest, Bitmap>() {

    override fun registerActivityResult(
        context: Context,
        activityResultRegistry: ActivityResultRegistry
    ): ActivityResultLauncher<PickVisualMediaRequest> = activityResultRegistry.register(
        PICK_GALLERY_IMAGE_KEY,
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        val callbackData = callback ?: return@register
        callback = null

        if (uri == null) {
            callbackData.callback.invoke(Result.failure(CanceledException()))
            return@register
        }

        processResult(
            context = context,
            callback = callbackData.callback,
            uri = uri,
            maxImageWidth = callbackData.maxWidth,
            maxImageHeight = callbackData.maxHeight,
        )
    }

    fun pick(
        maxWidth: Int,
        maxHeight: Int,
        callback: (Result<Bitmap>) -> Unit,
    ) {
        super.pick(callback)

        this.callback = GalleryPickerCallbackData(
            callback,
            maxWidth,
            maxHeight,
        )

        pickerLauncherHolder.value?.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    override fun createCallback(
        callback: (Result<Bitmap>) -> Unit,
    ): GalleryPickerCallbackData = GalleryPickerCallbackData(
        callback,
        0,
        0,
    )

    override fun launchActivityResult() = Unit

    class GalleryPickerCallbackData(
        override val callback: (Result<Bitmap>) -> Unit,
        val maxWidth: Int,
        val maxHeight: Int,
    ) : CallbackData<Bitmap>()

    companion object {
        private const val PICK_GALLERY_IMAGE_KEY = "PickGalleryImageKey"
    }
}
