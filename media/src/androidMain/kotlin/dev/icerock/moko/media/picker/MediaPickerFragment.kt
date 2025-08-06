/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import dev.icerock.moko.media.Media
import dev.icerock.moko.media.MediaFactory

class MediaPickerFragment : Fragment() {

    private val codeCallbackMap = mutableMapOf<Int, CallbackData>()

    init {
        @Suppress("DEPRECATION")
        retainInstance = true
    }

    fun pickVideo(callback: (Result<Media>) -> Unit) {
        val requestCode = codeCallbackMap.keys.maxOrNull() ?: 0

        codeCallbackMap[requestCode] = CallbackData(callback)

        val intent = Intent().apply {
            type = "video/*"
            action = Intent.ACTION_GET_CONTENT
        }

        startActivityForResult(intent, requestCode)
    }

    fun pickMedia(callback: (Result<Media>) -> Unit) {
        val requestCode = codeCallbackMap.keys.maxOrNull() ?: 0

        codeCallbackMap[requestCode] = CallbackData(callback)

        val intent = Intent().apply {
            type = "image/* video/*"
            action = Intent.ACTION_GET_CONTENT
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*", "image/*"))
        }
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val callbackData: CallbackData? = codeCallbackMap[requestCode]
        if (callbackData == null) return

        codeCallbackMap.remove(requestCode)

        val callback = callbackData.callback

        if (resultCode == Activity.RESULT_CANCELED) {
            callback.invoke(Result.failure(CanceledException()))
            return
        }

        processResult(callback, data)
    }

    @Suppress("ReturnCount")
    private fun processResult(
        callback: (Result<Media>) -> Unit,
        intent: Intent?
    ) {
        val context = this.context
        if (context == null) {
            callback(Result.failure(IllegalStateException("context unavailable")))
            return
        }
        if (intent == null) {
            callback(Result.failure(IllegalStateException("intent unavailable")))
            return
        }
        val intentData = intent.data
        if (intentData == null) {
            callback(Result.failure(IllegalStateException("intentData unavailable")))
            return
        }

        val result = runCatching {
            MediaFactory.create(context, intentData)
        }
        callback.invoke(result)
    }

    class CallbackData(val callback: (Result<Media>) -> Unit)
}
