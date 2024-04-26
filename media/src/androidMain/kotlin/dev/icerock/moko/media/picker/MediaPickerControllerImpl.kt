/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import android.app.Activity
import android.net.Uri
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dev.icerock.moko.media.Bitmap
import dev.icerock.moko.media.FileMedia
import dev.icerock.moko.media.Media
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import kotlin.coroutines.suspendCoroutine

internal class MediaPickerControllerImpl(
    override val permissionsController: PermissionsController,
) : MediaPickerController {

    private val activityHolder = MutableStateFlow<Activity?>(null)

    private var photoFilePath: String? = null

    private val imagePickerDelegate = ImagePickerDelegate()
    private val mediaPickerDelegate = MediaPickerDelegate()
    private val filePickerDelegate = FilePickerDelegate()

    override fun bind(activity: ComponentActivity) {
        this.activityHolder.value = activity
        permissionsController.bind(activity)

        imagePickerDelegate.bind(activity)
        mediaPickerDelegate.bind(activity)
        filePickerDelegate.bind(activity)

        val observer = object : LifecycleEventObserver {

            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    this@MediaPickerControllerImpl.activityHolder.value = null
                    source.lifecycle.removeObserver(this)
                }
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
                MediaSource.GALLERY -> imagePickerDelegate.pickGalleryImage(
                    maxWidth,
                    maxHeight,
                    action,
                )

                MediaSource.CAMERA -> imagePickerDelegate.pickCameraImage(
                    maxWidth,
                    maxHeight,
                    action,
                    outputUri,
                )
            }
        }

        return Bitmap(bitmap)
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
            mediaPickerDelegate.pickMedia(action)
        }
    }

    override suspend fun pickFiles(): FileMedia {
        permissionsController.providePermission(Permission.STORAGE)

        val path = suspendCoroutine<FileMedia> { continuation ->
            val action: (Result<FileMedia>) -> Unit = { continuation.resumeWith(it) }
            filePickerDelegate.pickFile(action)
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
