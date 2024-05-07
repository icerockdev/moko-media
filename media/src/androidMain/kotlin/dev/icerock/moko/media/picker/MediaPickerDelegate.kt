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
    PickerDelegate<MediaPickerDelegate.CallbackData, PickVisualMediaRequest>() {

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

    fun pick(callback: (Result<Media>) -> Unit) {
        this.callback?.let {
            it.callback.invoke(Result.failure(IllegalStateException("Callback should be null")))
            this.callback = null
        }
        this.callback = CallbackData(callback)

        pickerLauncherHolder.value?.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
        )
    }

    class CallbackData(val callback: (Result<Media>) -> Unit)

    companion object {
        private const val PICK_MEDIA_KEY = "PickMediaKey"
    }
}
