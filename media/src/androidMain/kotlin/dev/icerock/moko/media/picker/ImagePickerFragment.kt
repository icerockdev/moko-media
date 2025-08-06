/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import dev.icerock.moko.media.BitmapUtils
import java.io.File

class ImagePickerFragment : Fragment() {
    init {
        @Suppress("DEPRECATION")
        retainInstance = true
    }

    private val codeCallbackMap = mutableMapOf<Int, CallbackData>()

    private val maxImageWidth
        get() =
            arguments?.getInt(ARG_IMG_MAX_WIDTH, DEFAULT_MAX_IMAGE_WIDTH)
                ?: DEFAULT_MAX_IMAGE_WIDTH
    private val maxImageHeight
        get() =
            arguments?.getInt(ARG_IMG_MAX_HEIGHT, DEFAULT_MAX_IMAGE_HEIGHT)
                ?: DEFAULT_MAX_IMAGE_HEIGHT

    private var photoFilePath: String? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(PHOTO_FILE_PATH_KEY, photoFilePath)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        photoFilePath = savedInstanceState?.getString(PHOTO_FILE_PATH_KEY)
    }

    fun pickGalleryImage(callback: (Result<android.graphics.Bitmap>) -> Unit) {
        val requestCode = codeCallbackMap.keys.sorted().lastOrNull() ?: 0

        codeCallbackMap[requestCode] =
            CallbackData.Gallery(
                callback
            )

        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(intent, requestCode)
    }

    fun pickCameraImage(callback: (Result<android.graphics.Bitmap>) -> Unit) {
        val requestCode = codeCallbackMap.keys.sorted().lastOrNull() ?: 0

        val outputUri = createPhotoUri()
        codeCallbackMap[requestCode] =
            CallbackData.Camera(
                callback,
                outputUri
            )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            .putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
        startActivityForResult(intent, requestCode)
    }

    private fun createPhotoUri(): Uri {
        val context = requireContext()
        val filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val tmpFile = File(filesDir, DEFAULT_FILE_NAME)
        photoFilePath = tmpFile.absolutePath

        return FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + FILE_PROVIDER_SUFFIX,
            tmpFile
        )
    }

    @Suppress("UnreachableCode")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val callbackData = codeCallbackMap[requestCode] ?: return
        codeCallbackMap.remove(requestCode)

        val callback = callbackData.callback

        if (resultCode == Activity.RESULT_CANCELED) {
            callback.invoke(Result.failure(CanceledException()))
            return
        }

        when (callbackData) {
            is CallbackData.Gallery -> {
                val uri = data?.data
                if (uri != null) {
                    processResult(callback, uri)
                } else {
                    callback.invoke(Result.failure(IllegalArgumentException(data?.toString())))
                }
            }
            is CallbackData.Camera -> {
                processResult(callback, callbackData.outputUri)
            }
        }
    }

    @Suppress("ReturnCount")
    private fun processResult(
        callback: (Result<android.graphics.Bitmap>) -> Unit,
        uri: Uri
    ) {
        val contentResolver = requireContext().contentResolver

        val bitmapOptions = contentResolver.openInputStream(uri)?.use {
            BitmapUtils.getBitmapOptionsFromStream(it)
        } ?: run {
            callback.invoke(Result.failure(NoAccessToFileException(uri.toString())))
            return
        }

        val sampleSize =
            BitmapUtils.calculateInSampleSize(bitmapOptions, maxImageWidth, maxImageHeight)

        val orientation = contentResolver.openInputStream(uri)?.use {
            BitmapUtils.getBitmapOrientation(it)
        } ?: run {
            callback.invoke(Result.failure(NoAccessToFileException(uri.toString())))
            return
        }

        val bitmap = contentResolver.openInputStream(uri)?.use {
            BitmapUtils.getNormalizedBitmap(it, orientation, sampleSize)
        } ?: run {
            callback.invoke(Result.failure(NoAccessToFileException(uri.toString())))
            return
        }

        callback.invoke(Result.success(bitmap))
    }

    sealed class CallbackData(val callback: (Result<android.graphics.Bitmap>) -> Unit) {
        class Gallery(callback: (Result<android.graphics.Bitmap>) -> Unit) :
            CallbackData(callback)

        class Camera(
            callback: (Result<android.graphics.Bitmap>) -> Unit,
            val outputUri: Uri
        ) : CallbackData(callback)
    }

    companion object {
        private const val DEFAULT_FILE_NAME = "image.png"
        private const val PHOTO_FILE_PATH_KEY = "photoFilePath"
        private const val FILE_PROVIDER_SUFFIX = ".moko.media.provider"

        private const val ARG_IMG_MAX_WIDTH = "args_img_max_width"
        private const val ARG_IMG_MAX_HEIGHT = "args_img_max_height"

        fun newInstance(maxWidth: Int, maxHeight: Int): ImagePickerFragment {
            val pickerFragment = ImagePickerFragment()
            pickerFragment.arguments = Bundle().apply {
                putInt(ARG_IMG_MAX_WIDTH, maxWidth)
                putInt(ARG_IMG_MAX_HEIGHT, maxHeight)
            }
            return pickerFragment
        }
    }
}
