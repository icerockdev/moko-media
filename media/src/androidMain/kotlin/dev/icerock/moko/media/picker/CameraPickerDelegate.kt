/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts

internal class CameraPickerDelegate :
    ImagePickerDelegate<CameraPickerDelegate.CallbackData, Uri>() {

    override fun registerActivityResult(
        context: Context,
        activityResultRegistry: ActivityResultRegistry
    ): ActivityResultLauncher<Uri> = activityResultRegistry.register(
        PICK_CAMERA_IMAGE_KEY,
        ActivityResultContracts.TakePicture(),
    ) { result ->
        val callbackData = callback ?: return@register
        callback = null

        if (!result) {
            callbackData.callback.invoke(Result.failure(CanceledException()))
            return@register
        }

        processResult(
            context = context,
            callback = callbackData.callback,
            uri = callbackData.outputUri,
            maxImageWidth = callbackData.maxWidth,
            maxImageHeight = callbackData.maxHeight,
        )
    }

    fun pick(
        maxWidth: Int,
        maxHeight: Int,
        callback: (Result<android.graphics.Bitmap>) -> Unit,
        outputUri: Uri,
    ) {
        this.callback?.let {
            it.callback.invoke(Result.failure(IllegalStateException("Callback should be null")))
            this.callback = null
        }
        this.callback = CallbackData(
            callback,
            outputUri,
            maxWidth,
            maxHeight,
        )

        pickerLauncherHolder.value?.launch(
            outputUri
        )
    }

    class CallbackData(
        val callback: (Result<android.graphics.Bitmap>) -> Unit,
        val outputUri: Uri,
        val maxWidth: Int,
        val maxHeight: Int,
    )

    companion object {
        private const val PICK_CAMERA_IMAGE_KEY = "PickCameraImageKey"
    }
}
