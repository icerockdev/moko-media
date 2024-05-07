/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

internal class GalleryPickerDelegate :
    ImagePickerDelegate<GalleryPickerDelegate.CallbackData, PickVisualMediaRequest>() {

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
        callback: (Result<android.graphics.Bitmap>) -> Unit,
    ) {
        this.callback?.let {
            it.callback.invoke(Result.failure(IllegalStateException("Callback should be null")))
            this.callback = null
        }

        this.callback = CallbackData(
            callback,
            maxWidth,
            maxHeight,
        )

        pickerLauncherHolder.value?.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    class CallbackData(
        val callback: (Result<android.graphics.Bitmap>) -> Unit,
        val maxWidth: Int,
        val maxHeight: Int,
    )

    companion object {
        private const val PICK_GALLERY_IMAGE_KEY = "PickGalleryImageKey"
    }
}
