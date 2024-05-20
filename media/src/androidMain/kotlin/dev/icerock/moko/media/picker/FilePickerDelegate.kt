/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import dev.icerock.moko.media.FileMedia
import java.io.File

internal class FilePickerDelegate :
    PickerDelegate<FilePickerDelegate.FilePickerCallbackData, Array<String>, FileMedia>() {

    override fun registerActivityResult(
        context: Context,
        activityResultRegistry: ActivityResultRegistry
    ): ActivityResultLauncher<Array<String>> = activityResultRegistry.register(
        PICK_FILE_KEY,
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        val callbackData = callback ?: return@register
        callback = null

        val callback = callbackData.callback

        if (uri == null) {
            callback.invoke(Result.failure(CanceledException()))
            return@register
        }

        val path = uri.path
        if (path == null) {
            callback.invoke(Result.failure(java.lang.IllegalStateException("File is null")))
            return@register
        }

        @SuppressLint("Range")
        val fileNameWithExtension = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (!it.moveToFirst()) null
                else it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        } else null

        val file = File(path)
        val name = file.name
        val result = Result.success(
            FileMedia(
                fileNameWithExtension ?: name,
                uri.toString(),
            )
        )
        callback.invoke(result)
    }

    override fun createCallback(
        callback: (Result<FileMedia>) -> Unit,
        mediaOptions: MediaOptions?
    ): FilePickerCallbackData = FilePickerCallbackData(callback)

    override fun launchActivityResult(mediaOptions: MediaOptions?) {
        pickerLauncherHolder.value?.launch(
            arrayOf(
                "*/*",
            )
        )
    }

    class FilePickerCallbackData(
        override val callback: (Result<FileMedia>) -> Unit
    ) : CallbackData<FileMedia>

    companion object {
        private const val PICK_FILE_KEY = "PickFileKey"
    }
}
