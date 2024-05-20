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

    override fun createCallback(
        callback: (Result<Bitmap>) -> Unit,
        mediaOptions: MediaOptions?
    ): GalleryPickerCallbackData {
        val galleryPickerMediaOptions = mediaOptions as? GalleryPickerMediaOptions
        val maxWidth = galleryPickerMediaOptions?.maxWidth ?: 0
        val maxHeight = galleryPickerMediaOptions?.maxHeight ?: 0
        return GalleryPickerCallbackData(
            callback,
            maxWidth,
            maxHeight,
        )
    }

    override fun launchActivityResult(mediaOptions: MediaOptions?) {
        pickerLauncherHolder.value?.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    class GalleryPickerCallbackData(
        override val callback: (Result<Bitmap>) -> Unit,
        val maxWidth: Int,
        val maxHeight: Int,
    ) : CallbackData<Bitmap>

    class GalleryPickerMediaOptions(
        val maxWidth: Int,
        val maxHeight: Int,
    ) : MediaOptions

    companion object {
        private const val PICK_GALLERY_IMAGE_KEY = "PickGalleryImageKey"
    }
}
