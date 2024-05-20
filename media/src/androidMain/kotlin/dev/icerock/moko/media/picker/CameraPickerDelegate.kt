/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts

internal class CameraPickerDelegate :
    ImagePickerDelegate<CameraPickerDelegate.CameraPickerCallbackData, Uri, Bitmap>() {

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

    override fun createCallback(
        callback: (Result<Bitmap>) -> Unit,
        mediaOptions: MediaOptions?
    ): CameraPickerCallbackData {
        val cameraPickerMediaOptions = mediaOptions as? CameraPickerMediaOptions
        val outputUri = cameraPickerMediaOptions?.outputUri ?: Uri.EMPTY
        val maxWidth = cameraPickerMediaOptions?.maxWidth ?: 0
        val maxHeight = cameraPickerMediaOptions?.maxHeight ?: 0
        return CameraPickerCallbackData(
            callback,
            outputUri,
            maxWidth,
            maxHeight,
        )
    }

    override fun launchActivityResult(mediaOptions: MediaOptions?) {
        val cameraPickerMediaOptions = mediaOptions as? CameraPickerMediaOptions
        val outputUri = cameraPickerMediaOptions?.outputUri ?: Uri.EMPTY
        pickerLauncherHolder.value?.launch(
            outputUri
        )
    }

    class CameraPickerCallbackData(
        override val callback: (Result<Bitmap>) -> Unit,
        val outputUri: Uri,
        val maxWidth: Int,
        val maxHeight: Int,
    ) : CallbackData<Bitmap>

    class CameraPickerMediaOptions(
        val outputUri: Uri,
        val maxWidth: Int,
        val maxHeight: Int,
    ) : MediaOptions

    companion object {
        private const val PICK_CAMERA_IMAGE_KEY = "PickCameraImageKey"
    }
}
