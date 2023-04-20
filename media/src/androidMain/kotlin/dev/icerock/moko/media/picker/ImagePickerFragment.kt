/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import dev.icerock.moko.media.BitmapUtils
import java.io.File

class ImagePickerFragment : Fragment() {
    init {
        @Suppress("DEPRECATION")
        retainInstance = true
    }

    private var callback: CallbackData? = null

    private val maxImageWidth
        get() =
            arguments?.getInt(ARG_IMG_MAX_WIDTH, DEFAULT_MAX_IMAGE_WIDTH)
                ?: DEFAULT_MAX_IMAGE_WIDTH
    private val maxImageHeight
        get() =
            arguments?.getInt(ARG_IMG_MAX_HEIGHT, DEFAULT_MAX_IMAGE_HEIGHT)
                ?: DEFAULT_MAX_IMAGE_HEIGHT

    private var photoFilePath: String? = null

    private val galleryImageResult = registerForActivityResult(ActivityResultContracts.PickVisualMedia()){ uri ->
        val callbackData = callback ?: return@registerForActivityResult
        callback = null

        val callback = callbackData.callback

        if (uri == null) {
            callback.invoke(Result.failure(CanceledException()))
            return@registerForActivityResult
        }

        processResult(callback, uri)
    }

    private val cameraImageResult = registerForActivityResult(ActivityResultContracts.TakePicture()){ result ->
        val callbackData = callback ?: return@registerForActivityResult
        callback = null

        if (callbackData !is CallbackData.Camera){
            callbackData.callback.invoke(
                Result.failure(
                    java.lang.IllegalStateException("Callback type should be Camera")
                )
            )
            return@registerForActivityResult
        }

        if (!result){
            callbackData.callback.invoke(Result.failure(CanceledException()))
            return@registerForActivityResult
        }

        processResult(callbackData.callback, callbackData.outputUri)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(PHOTO_FILE_PATH_KEY, photoFilePath)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        photoFilePath = savedInstanceState?.getString(PHOTO_FILE_PATH_KEY)
    }

    fun pickGalleryImage(callback: (Result<android.graphics.Bitmap>) -> Unit) {
        this.callback?.let {
            it.callback.invoke(Result.failure(IllegalStateException("Callback should be null")))
            this.callback = null
        }

        this.callback = CallbackData.Gallery(callback)

        galleryImageResult.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    fun pickCameraImage(callback: (Result<android.graphics.Bitmap>) -> Unit) {
        this.callback?.let {
            it.callback.invoke(Result.failure(IllegalStateException("Callback should be null")))
            this.callback = null
        }

        val outputUri = createPhotoUri()
        this.callback = CallbackData.Camera(callback, outputUri)

        cameraImageResult.launch(outputUri)
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
