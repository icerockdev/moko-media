/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dev.icerock.moko.media.Media
import dev.icerock.moko.media.MediaFactory
import kotlinx.coroutines.flow.MutableStateFlow

internal class MediaPickerDelegate {

    private var callback: CallbackData? = null

    private val mediaPickerLauncherHolder =
        MutableStateFlow<ActivityResultLauncher<PickVisualMediaRequest>?>(null)

    fun bind(activity: ComponentActivity) {
        val activityResultRegistryOwner = activity as ActivityResultRegistryOwner
        val activityResultRegistry = activityResultRegistryOwner.activityResultRegistry

        mediaPickerLauncherHolder.value = activityResultRegistry.register(
            PICK_MEDIA_KEY,
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            val callbackData = callback ?: return@register
            callback = null

            val callback = callbackData.callback

            if (uri == null) {
                callback.invoke(Result.failure(CanceledException()))
                return@register
            }

            val result = kotlin.runCatching {
                MediaFactory.create(activity, uri)
            }
            callback.invoke(result)
        }

        val observer = object : LifecycleEventObserver {

            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    mediaPickerLauncherHolder.value = null
                    source.lifecycle.removeObserver(this)
                }
            }
        }
        activity.lifecycle.addObserver(observer)
    }

    fun pickMedia(callback: (Result<Media>) -> Unit) {
        this.callback?.let {
            it.callback.invoke(Result.failure(IllegalStateException("Callback should be null")))
            this.callback = null
        }
        this.callback = CallbackData(callback)

        mediaPickerLauncherHolder.value?.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
        )
    }

    class CallbackData(val callback: (Result<Media>) -> Unit)

    companion object {
        private const val PICK_MEDIA_KEY = "PickMediaKey"
    }
}
