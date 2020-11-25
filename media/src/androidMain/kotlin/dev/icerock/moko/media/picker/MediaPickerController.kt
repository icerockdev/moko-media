/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import com.nbsp.materialfilepicker.ui.FilePickerActivity.ARG_CLOSEABLE
import dev.icerock.moko.media.Bitmap
import dev.icerock.moko.media.BitmapUtils
import dev.icerock.moko.media.BitmapUtils.calculateInSampleSize
import dev.icerock.moko.media.BitmapUtils.getBitmapForStream
import dev.icerock.moko.media.BitmapUtils.getBitmapOptionsFromStream
import dev.icerock.moko.media.FileMedia
import dev.icerock.moko.media.Media
import dev.icerock.moko.media.MediaFactory
import dev.icerock.moko.media.picker.MediaPickerController.PickerFragment.Companion.ARG_IMG_MAX_HEIGHT
import dev.icerock.moko.media.picker.MediaPickerController.PickerFragment.Companion.ARG_IMG_MAX_WIDTH
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import java.io.File
import java.io.InputStream
import kotlin.coroutines.suspendCoroutine

actual class MediaPickerController(
    val permissionsController: PermissionsController,
    val pickerFragmentTag: String = "MediaControllerPicker",
    val filePickerFragmentTag: String = "FileMediaControllerPicker"
) {
    var fragmentManager: FragmentManager? = null

    fun bind(lifecycle: Lifecycle, fragmentManager: FragmentManager) {
        permissionsController.bind(lifecycle, fragmentManager)

        this.fragmentManager = fragmentManager

        val observer = object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroyed(source: LifecycleOwner) {
                this@MediaPickerController.fragmentManager = null
                source.lifecycle.removeObserver(this)
            }
        }
        lifecycle.addObserver(observer)
    }

    actual suspend fun pickImage(source: MediaSource): Bitmap {
        return pickImage(source, DEFAULT_MAX_IMAGE_WIDTH, DEFAULT_MAX_IMAGE_HEIGHT)
    }

    /**
     * A default values for [maxWidth] and [maxHeight] arguments are not used because bug of kotlin
     * compiler. Default values for suspend functions don't work correctly.
     * (Look here: https://youtrack.jetbrains.com/issue/KT-37331)
     */
    actual suspend fun pickImage(source: MediaSource, maxWidth: Int, maxHeight: Int): Bitmap {
        val fragmentManager =
            fragmentManager ?: throw IllegalStateException("can't pick image without active window")

        source.requiredPermissions().forEach { permission ->
            permissionsController.providePermission(permission)
        }

        val currentFragment: Fragment? = fragmentManager.findFragmentByTag(pickerFragmentTag)
        val pickerFragment: PickerFragment = if (currentFragment != null) {
            currentFragment as PickerFragment
        } else {
            PickerFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_IMG_MAX_WIDTH, maxWidth)
                    putInt(ARG_IMG_MAX_HEIGHT, maxHeight)
                }
                fragmentManager
                    .beginTransaction()
                    .add(this, pickerFragmentTag)
                    .commitNow()
            }
        }

        val bitmap = suspendCoroutine<android.graphics.Bitmap> { continuation ->
            val action: (Result<android.graphics.Bitmap>) -> Unit = { continuation.resumeWith(it) }
            when (source) {
                MediaSource.GALLERY -> pickerFragment.pickGalleryImage(action)
                MediaSource.CAMERA -> pickerFragment.pickCameraImage(action)
            }
        }

        return Bitmap(bitmap)
    }

    actual suspend fun pickMedia(): Media {
        val fragmentManager =
            fragmentManager ?: throw IllegalStateException("can't pick image without active window")

        permissionsController.providePermission(Permission.GALLERY)

        val currentFragment: Fragment? = fragmentManager.findFragmentByTag(pickerFragmentTag)
        val pickerFragment: MediaPickerFragment = if (currentFragment != null) {
            currentFragment as MediaPickerFragment
        } else {
            MediaPickerFragment().apply {
                fragmentManager
                    .beginTransaction()
                    .add(this, pickerFragmentTag)
                    .commitNow()
            }
        }

        return suspendCoroutine { continuation ->
            val action: (Result<Media>) -> Unit = { continuation.resumeWith(it) }
            pickerFragment.pickMedia(action)
        }
    }

    actual suspend fun pickFiles(): FileMedia {
        val fragmentManager =
            fragmentManager ?: throw IllegalStateException("can't pick image without active window")


        permissionsController.providePermission(Permission.STORAGE)

        val currentFragment: Fragment? = fragmentManager.findFragmentByTag(filePickerFragmentTag)
        val pickerFragment: FilePickerFragment = if (currentFragment != null) {
            currentFragment as FilePickerFragment
        } else {
            FilePickerFragment().apply {
                fragmentManager
                    .beginTransaction()
                    .add(this, pickerFragmentTag)
                    .commitNow()
            }
        }

        val path = suspendCoroutine<FileMedia> { continuation ->
            val action: (Result<FileMedia>) -> Unit = { continuation.resumeWith(it) }
            pickerFragment.pickFile(action)
        }

        return path
    }

    private fun MediaSource.requiredPermissions(): List<Permission> {
        return when (this) {
            MediaSource.GALLERY -> listOf(Permission.GALLERY)
            MediaSource.CAMERA -> listOf(Permission.CAMERA)
        }
    }

    class PickerFragment : Fragment() {
        init {
            retainInstance = true
        }

        private val codeCallbackMap = mutableMapOf<Int, CallbackData>()

        private var maxImageWidth = DEFAULT_MAX_IMAGE_WIDTH
        private var maxImageHeight = DEFAULT_MAX_IMAGE_HEIGHT

        private var photoFilePath: String? = null

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            maxImageWidth = arguments?.getInt(ARG_IMG_MAX_WIDTH, DEFAULT_MAX_IMAGE_WIDTH)
                ?: DEFAULT_MAX_IMAGE_WIDTH
            maxImageHeight = arguments?.getInt(ARG_IMG_MAX_HEIGHT, DEFAULT_MAX_IMAGE_HEIGHT)
                ?: DEFAULT_MAX_IMAGE_HEIGHT
            photoFilePath = savedInstanceState?.getString(PHOTO_FILE_PATH_KEY)
        }

        override fun onSaveInstanceState(outState: Bundle) {
            outState.putString(PHOTO_FILE_PATH_KEY, photoFilePath)
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
            val tmpFile = File(
                filesDir,
                DEFAULT_FILE_NAME
            )
            photoFilePath = tmpFile.absolutePath

            return FileProvider.getUriForFile(context, context.applicationContext.packageName + FILE_PROVIDER_SUFFIX, tmpFile)
        }

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
                is CallbackData.Gallery -> processGalleryResult(callback, data)
                is CallbackData.Camera -> processCameraResult(
                    callback,
                    callbackData.outputUri
                )
            }
        }

        private fun processGalleryResult(
            callback: (Result<android.graphics.Bitmap>) -> Unit,
            data: Intent?
        ) {
            val uri = data?.data
            if (uri == null) {
                callback.invoke(Result.failure(IllegalArgumentException(data?.toString())))
                return
            }

            val contentResolver = requireContext().contentResolver
            var inputStream = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                callback.invoke(Result.failure(NoAccessToFileException(uri.toString())))
                return
            }

            val bitmapOptions = getBitmapOptionsFromStream(inputStream)
            inputStream.close()

            inputStream = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                callback.invoke(Result.failure(NoAccessToFileException(uri.toString())))
                return
            }

            val sampleSize = calculateInSampleSize(bitmapOptions, maxImageWidth, maxImageHeight)
            val bitmap = getBitmapForStream(inputStream, sampleSize)
            inputStream.close()

            if (bitmap != null) {
                callback.invoke(Result.success(bitmap))
            } else {
                callback.invoke(
                    Result.failure(BitmapDecodeException("The image data could not be decoded."))
                )
            }
        }

        private fun processCameraResult(
            callback: (Result<android.graphics.Bitmap>) -> Unit,
            outputUri: Uri
        ) {
            val contentResolver = requireContext().contentResolver
            val inputStream = contentResolver.openInputStream(outputUri)
            if (inputStream == null) {
                callback.invoke(Result.failure(NoAccessToFileException(outputUri.toString())))
                return
            }
            val bitmap = decodeImage(photoFilePath.orEmpty(), inputStream)
            callback.invoke(Result.success(bitmap))
        }

        private fun decodeImage(
            filename: String,
            inputStream: InputStream
        ): android.graphics.Bitmap {
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val angle = BitmapUtils.getAngle(filename)
            return BitmapUtils.cloneRotated(bitmap, angle)
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

            internal const val ARG_IMG_MAX_WIDTH = "args_img_max_width"
            internal const val ARG_IMG_MAX_HEIGHT = "args_img_max_height"
        }
    }

    class MediaPickerFragment : Fragment() {

        private val codeCallbackMap = mutableMapOf<Int, CallbackData>()

        init {
            retainInstance = true
        }

        fun pickVideo(callback: (Result<Media>) -> Unit) {
            val requestCode = codeCallbackMap.keys.sorted().lastOrNull() ?: 0

            codeCallbackMap[requestCode] = CallbackData(callback)

            val intent = Intent().apply {
                type = "video/*"
                action = Intent.ACTION_GET_CONTENT
            }

            startActivityForResult(intent, requestCode)
        }

        fun pickMedia(callback: (Result<Media>) -> Unit) {
            val requestCode = codeCallbackMap.keys.sorted().lastOrNull() ?: 0

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

            val callbackData = codeCallbackMap[requestCode] ?: return
            codeCallbackMap.remove(requestCode)

            val callback = callbackData.callback

            if (resultCode == Activity.RESULT_CANCELED) {
                callback.invoke(Result.failure(CanceledException()))
                return
            }

            processResult(callback, data)
        }

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

            val result = kotlin.runCatching {
                MediaFactory.create(context, intentData)
            }
            callback.invoke(result)
        }

        class CallbackData(val callback: (Result<Media>) -> Unit)
    }

    class FilePickerFragment : Fragment() {
        init {
            retainInstance = true
        }

        private val codeCallbackMap = mutableMapOf<Int, CallbackData>()

        fun pickFile(callback: (Result<FileMedia>) -> Unit) {
            val requestCode = codeCallbackMap.keys.sorted().lastOrNull() ?: 0

            codeCallbackMap[requestCode] = CallbackData(callback)

            // TODO нужно убрать использование внешней зависимости, сделать конфигурацию способа
            //  выбора файла из вне (аргументом в контроллер передавать)
            val intent = Intent(requireContext(), FilePickerActivity::class.java)
            intent.putExtra(ARG_CLOSEABLE, true)
            startActivityForResult(intent, requestCode)
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

            val callbackData = codeCallbackMap[requestCode] ?: return
            codeCallbackMap.remove(requestCode)

            val callback = callbackData.callback

            if (resultCode == Activity.RESULT_CANCELED) {
                callback.invoke(Result.failure(CanceledException()))
                return
            }

            processResult(callback, data)
        }

        private fun processResult(
            callback: (Result<FileMedia>) -> Unit,
            data: Intent?
        ) {
            val filePath = data?.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)

            filePath?.let { path ->
                val name = File(path).name
                callback(Result.success(FileMedia(name, path)))
            }
        }

        class CallbackData(val callback: (Result<FileMedia>) -> Unit)
    }
}
