/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import dev.icerock.moko.media.Bitmap
import dev.icerock.moko.media.BitmapUtils
import dev.icerock.moko.media.FileMedia
import dev.icerock.moko.media.Media
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.util.UUID
import kotlin.coroutines.suspendCoroutine

internal class MediaPickerControllerImpl(
    override val permissionsController: PermissionsController,
) : MediaPickerController {

    private val activityHolder = MutableStateFlow<Activity?>(null)

    private var photoFilePath: String? = null

    private val key = UUID.randomUUID().toString()

    private val codeCallbackMap = mutableMapOf<Int, CallbackData<*>>()

    private val takePictureLauncherHolder = MutableStateFlow<ActivityResultLauncher<Uri>?>(null)
    private val pickVisualMediaLauncherHolder = MutableStateFlow<ActivityResultLauncher<PickVisualMediaRequest>?>(null)
    private val pickFileMediaLauncherHolder = MutableStateFlow<ActivityResultLauncher<Array<String>>?>(null)

    private val maxImageWidth
        get() = DEFAULT_MAX_IMAGE_WIDTH
    private val maxImageHeight
        get() = DEFAULT_MAX_IMAGE_HEIGHT

    override fun bind(activity: ComponentActivity) {
        this.activityHolder.value = activity
        permissionsController.bind(activity)

        val activityResultRegistryOwner = activity as ActivityResultRegistryOwner

        val takePictureLauncher = activityResultRegistryOwner.activityResultRegistry.register(
            "TakePicture-$key",
            ActivityResultContracts.TakePicture()
        ) { success ->
            val callbackData = codeCallbackMap.values.last() as CallbackData<android.graphics.Bitmap>
            val callback = callbackData.callback
            if (success) {
                when (callbackData) {
                    is CallbackData.Camera -> {
                        processResult(activity, callback, callbackData.outputUri)
                    }
                    else -> Unit
                }
            } else {
                callback.invoke(Result.failure(CanceledException()))
            }
        }

        val pickVisualMediaLauncher = activityResultRegistryOwner.activityResultRegistry.register(
            "PickVisualMedia-$key",
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            val callbackData = codeCallbackMap.values.last() as CallbackData<android.graphics.Bitmap>
            val callback = callbackData.callback
            if (uri != null) {
                processResult(activity, callback, uri)
            } else {
                callback.invoke(Result.failure(CanceledException()))
            }
        }

        val pickFileMediaLauncher = activityResultRegistryOwner.activityResultRegistry.register(
            "PickFileMedia-$key",
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            val callbackData = codeCallbackMap.values.last() as CallbackData<FileMedia>
            val callback = callbackData.callback

            if (uri != null) {
                callback.invoke(Result.failure(CanceledException()))
                return@register
            }

            if (uri?.path == null) {
                callback.invoke(Result.failure(java.lang.IllegalStateException("File is null")))
                return@register
            }
            uri.path?.let { path ->
                // TODO pass result
            }
        }

        takePictureLauncherHolder.value = takePictureLauncher
        pickVisualMediaLauncherHolder.value = pickVisualMediaLauncher
        pickFileMediaLauncherHolder.value = pickFileMediaLauncher

        val observer = object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroyed(source: LifecycleOwner) {
                this@MediaPickerControllerImpl.activityHolder.value = null
                this@MediaPickerControllerImpl.takePictureLauncherHolder.value = null
                this@MediaPickerControllerImpl.pickVisualMediaLauncherHolder.value = null
                this@MediaPickerControllerImpl.pickFileMediaLauncherHolder.value = null
                source.lifecycle.removeObserver(this)
            }
        }
        activity.lifecycle.addObserver(observer)
    }

    override suspend fun pickImage(source: MediaSource): Bitmap {
        return pickImage(source, DEFAULT_MAX_IMAGE_WIDTH, DEFAULT_MAX_IMAGE_HEIGHT)
    }

    /**
     * A default values for [maxWidth] and [maxHeight] arguments are not used because bug of kotlin
     * compiler. Default values for suspend functions don't work correctly.
     * (Look here: https://youtrack.jetbrains.com/issue/KT-37331)
     */
    override suspend fun pickImage(source: MediaSource, maxWidth: Int, maxHeight: Int): Bitmap {
        source.requiredPermissions().forEach { permission ->
            permissionsController.providePermission(permission)
        }

        val outputUri = createPhotoUri()

        val bitmap = suspendCoroutine<android.graphics.Bitmap> { continuation ->
            val action: (Result<android.graphics.Bitmap>) -> Unit = { continuation.resumeWith(it) }
            when (source) {
                MediaSource.GALLERY -> pickGalleryImage(action)
                MediaSource.CAMERA -> pickCameraImage(outputUri, action)
            }
        }

        return Bitmap(bitmap)
    }

    private fun pickGalleryImage(callback: (Result<android.graphics.Bitmap>) -> Unit) {
        val requestCode = codeCallbackMap.keys.sorted().lastOrNull() ?: 0
        codeCallbackMap[requestCode] =
            CallbackData.Gallery(
                callback
            )
        val launcher = pickVisualMediaLauncherHolder.value
        launcher?.launch(PickVisualMediaRequest())
    }

    private fun pickCameraImage(outputUri: Uri, callback: (Result<android.graphics.Bitmap>) -> Unit) {
        val requestCode = codeCallbackMap.keys.sorted().lastOrNull() ?: 0
        codeCallbackMap[requestCode] =
            CallbackData.Camera(
                callback,
                outputUri
            )

        val launcher = takePictureLauncherHolder.value
        launcher?.launch(outputUri)
    }

    private fun pickMediaFile(callback: (Result<Media>) -> Unit) {
        val requestCode = codeCallbackMap.keys.sorted().lastOrNull() ?: 0
        codeCallbackMap[requestCode] =
            CallbackData.Media(
                callback
            )
        val launcher = pickVisualMediaLauncherHolder.value
        launcher?.launch(PickVisualMediaRequest())
    }

    private suspend fun createPhotoUri(): Uri {
        val context = awaitActivity()
        val filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val tmpFile = File(filesDir, DEFAULT_FILE_NAME)
        photoFilePath = tmpFile.absolutePath

        return FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + FILE_PROVIDER_SUFFIX,
            tmpFile
        )
    }

    override suspend fun pickMedia(): Media {
        permissionsController.providePermission(Permission.GALLERY)

        return suspendCoroutine { continuation ->
            val action: (Result<Media>) -> Unit = { continuation.resumeWith(it) }
            pickMediaFile(action)
        }
    }

    private fun pickFile(callback: (Result<FileMedia>) -> Unit) {
        val requestCode = codeCallbackMap.keys.sorted().lastOrNull() ?: 0
        codeCallbackMap[requestCode] =
            CallbackData.FileMedia(
                callback
            )
        val launcher = pickFileMediaLauncherHolder.value
        launcher?.launch(
            arrayOf(
                "application/pdf",
                "application/octet-stream",
                "application/doc",
                "application/msword",
                "application/ms-doc",
                "application/vnd.ms-excel",
                "application/vnd.ms-powerpoint",
                "application/json",
                "application/zip",
                "text/plain",
                "text/html",
                "text/xml",
                "audio/mpeg",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            )
        )
    }

    @Suppress("ReturnCount")
    private fun processResult(
        context: Context,
        callback: (Result<android.graphics.Bitmap>) -> Unit,
        uri: Uri
    ) {
        val contentResolver = context.contentResolver

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

    override suspend fun pickFiles(): FileMedia {
        permissionsController.providePermission(Permission.STORAGE)

        val path = suspendCoroutine<FileMedia> { continuation ->
            val action: (Result<FileMedia>) -> Unit = { continuation.resumeWith(it) }
            pickFile(action)
        }

        return path
    }

    private suspend fun awaitActivity(): Activity {
        val activity = activityHolder.value
        if (activity != null) return activity

        return withTimeoutOrNull(AWAIT_ACTIVITY_TIMEOUT_DURATION_MS) {
            activityHolder.filterNotNull().first()
        } ?: error(
            "activity is null, `bind` function was never called," +
                    " consider calling mediaPickerController.bind(activity)" +
                    " or BindMediaPickerEffect(mediaPickerController) in the composable function," +
                    " check the documentation for more info: " +
                    "https://github.com/icerockdev/moko-media/blob/master/README.md"
        )
    }

    private fun MediaSource.requiredPermissions(): List<Permission> {
        return when (this) {
            MediaSource.GALLERY -> listOf(Permission.GALLERY)
            MediaSource.CAMERA -> listOf(Permission.CAMERA)
        }
    }

    sealed class CallbackData<T>(val callback: (Result<T>) -> Unit) {
        class Gallery(callback: (Result<android.graphics.Bitmap>) -> Unit) :
            CallbackData<android.graphics.Bitmap>(callback)

        class Camera(
            callback: (Result<android.graphics.Bitmap>) -> Unit,
            val outputUri: Uri
        ) : CallbackData<android.graphics.Bitmap>(callback)

        class Media(
            callback: (Result<dev.icerock.moko.media.Media>) -> Unit,
        ) : CallbackData<dev.icerock.moko.media.Media>(callback)

        class FileMedia(
            callback: (Result<dev.icerock.moko.media.FileMedia>) -> Unit,
        ) : CallbackData<dev.icerock.moko.media.FileMedia>(callback)
    }

    companion object {
        private const val AWAIT_ACTIVITY_TIMEOUT_DURATION_MS = 2000L
        private const val DEFAULT_FILE_NAME = "image.png"
        private const val FILE_PROVIDER_SUFFIX = ".moko.media.provider"
    }
}
