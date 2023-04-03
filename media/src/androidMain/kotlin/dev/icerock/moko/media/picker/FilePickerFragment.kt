/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import dev.icerock.moko.media.FileMedia
import java.io.File

class FilePickerFragment : Fragment() {
    init {
        @Suppress("DEPRECATION")
        retainInstance = true
    }

    private val codeCallbackMap = mutableMapOf<Int, CallbackData>()

    private val pickDocument =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            val callbackData = codeCallbackMap[0] ?: return@registerForActivityResult
            codeCallbackMap.remove(0)

            val callback = callbackData.callback

            if (uri == null) {
                callback.invoke(Result.failure(CanceledException()))
                return@registerForActivityResult
            }

            if (uri.path == null) {
                callback.invoke(Result.failure(java.lang.IllegalStateException("File is null")))
                return@registerForActivityResult
            }

            uri.path?.let { path ->
                val name = File(path).name
                callback(Result.success(FileMedia(name, path)))
            }
        }


    fun pickFile(callback: (Result<FileMedia>) -> Unit) {
        val requestCode = codeCallbackMap.keys.maxOrNull() ?: 0

        codeCallbackMap[requestCode] = CallbackData(callback)

        pickDocument.launch(
            arrayOf(
                "application/pdf",
                "application/msword",
                "application/ms-doc",
                "application/doc",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "text/plain"
            )
        )
    }

    class CallbackData(val callback: (Result<FileMedia>) -> Unit)
}
