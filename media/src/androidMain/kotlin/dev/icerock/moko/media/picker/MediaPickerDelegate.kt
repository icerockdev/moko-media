/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import dev.icerock.moko.media.Media
import dev.icerock.moko.media.MediaFactory

internal class MediaPickerDelegate :
    PickerDelegate<MediaPickerDelegate.MediaPickerCallbackData, PickVisualMediaRequest, Media>() {

    override fun registerActivityResult(
        context: Context,
        activityResultRegistry: ActivityResultRegistry
    ): ActivityResultLauncher<PickVisualMediaRequest> = activityResultRegistry.register(
        PICK_MEDIA_KEY,
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        val callbackData = callback ?: return@register
        callback = null

        val callback = callbackData.callback

        if (uri == null) {
            callback.invoke(Result.failure(CanceledException()))
            return@register
        }

        val result = kotlin.runCatching {
            MediaFactory.create(context, uri)
        }
        callback.invoke(result)
    }

    override fun createCallback(
        callback: (Result<Media>) -> Unit,
    ): MediaPickerCallbackData = MediaPickerCallbackData(callback)

    override fun launchActivityResult() {
        pickerLauncherHolder.value?.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
        )
    }

    class MediaPickerCallbackData(
        override val callback: (Result<Media>) -> Unit,
    ) : CallbackData<Media>()

    companion object {
        private const val PICK_MEDIA_KEY = "PickMediaKey"
    }
}
