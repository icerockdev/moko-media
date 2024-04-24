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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
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
    private val mediaPickerFragmentTag: String,
    private val imagePickerFragmentTag: String,
    private val filePickerFragmentTag: String
) : MediaPickerController {

    private val activityHolder = MutableStateFlow<Activity?>(null)

    private var photoFilePath: String? = null

    var fragmentManager: FragmentManager? = null

    private val key = UUID.randomUUID().toString()

    // TODO replace ImagePickerFragment.CallbackData to own CallbackData
    private val codeCallbackMap = mutableMapOf<Int, ImagePickerFragment.CallbackData>()

    private val takePictureLauncherHolder = MutableStateFlow<ActivityResultLauncher<Uri>?>(null)
    private val pickMultipleVisualMediaLauncherHolder = MutableStateFlow<ActivityResultLauncher<PickVisualMediaRequest>?>(null)

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
            val callbackData = codeCallbackMap.values.last()
            val callback = callbackData.callback
            if (success) {
                when (callbackData) {
                    is ImagePickerFragment.CallbackData.Camera -> {
                        processResult(activity, callback, callbackData.outputUri)
                    }
                    else -> Unit
                }
            } else {
                callback.invoke(Result.failure(CanceledException()))
            }
        }

        val pickMultipleVisualMediaLauncher = activityResultRegistryOwner.activityResultRegistry.register(
            "PickVisualMedia-$key",
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            val callbackData = codeCallbackMap.values.last()
            val callback = callbackData.callback
            if (uri != null) {
                processResult(activity, callback, uri)
            } else {
                callback.invoke(Result.failure(CanceledException()))
            }
        }

        takePictureLauncherHolder.value = takePictureLauncher
        pickMultipleVisualMediaLauncherHolder.value = pickMultipleVisualMediaLauncher

        val observer = object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroyed(source: LifecycleOwner) {
                this@MediaPickerControllerImpl.activityHolder.value = null
                this@MediaPickerControllerImpl.takePictureLauncherHolder.value = null
                this@MediaPickerControllerImpl.pickMultipleVisualMediaLauncherHolder.value = null
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
            ImagePickerFragment.CallbackData.Gallery(
                callback
            )
        val launcher = pickMultipleVisualMediaLauncherHolder.value
        launcher?.launch(PickVisualMediaRequest())
    }

    private fun pickCameraImage(outputUri: Uri, callback: (Result<android.graphics.Bitmap>) -> Unit) {
        val requestCode = codeCallbackMap.keys.sorted().lastOrNull() ?: 0
        codeCallbackMap[requestCode] =
            ImagePickerFragment.CallbackData.Camera(
                callback,
                outputUri
            )

        val launcher = takePictureLauncherHolder.value
        launcher?.launch(outputUri)
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

    // TODO refactor fragmentManager usage
    override suspend fun pickMedia(): Media {
        val fragmentManager =
            fragmentManager ?: error("can't pick image without active window")

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
        val fragmentManager =
            fragmentManager ?: error("can't pick image without active window")

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

    companion object {
        private const val AWAIT_ACTIVITY_TIMEOUT_DURATION_MS = 2000L
        private const val DEFAULT_FILE_NAME = "image.png"
        private const val FILE_PROVIDER_SUFFIX = ".moko.media.provider"
    }
}
