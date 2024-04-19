/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import dev.icerock.moko.media.Bitmap
import dev.icerock.moko.media.FileMedia
import dev.icerock.moko.media.Media
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import kotlin.coroutines.suspendCoroutine

internal class MediaPickerControllerImpl(
    override val permissionsController: PermissionsController,
    private val mediaPickerFragmentTag: String,
    private val imagePickerFragmentTag: String,
    private val filePickerFragmentTag: String
) : MediaPickerController {
    var fragmentManager: FragmentManager? = null

    override fun bind(activity: FragmentActivity) {
        permissionsController.bind(activity)

        this.fragmentManager = activity.supportFragmentManager

        val observer = object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroyed(source: LifecycleOwner) {
                this@MediaPickerControllerImpl.fragmentManager = null
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
        val fragmentManager =
            fragmentManager ?: error("can't pick image without active window")

        source.requiredPermissions().forEach { permission ->
            permissionsController.providePermission(permission)
        }

        val currentFragment: Fragment? = fragmentManager.findFragmentByTag(imagePickerFragmentTag)
        val imagePickerFragment: ImagePickerFragment = if (currentFragment != null) {
            currentFragment as ImagePickerFragment
        } else {
            ImagePickerFragment.newInstance(maxWidth, maxHeight).also {
                fragmentManager
                    .beginTransaction()
                    .add(it, imagePickerFragmentTag)
                    .commitNow()
            }
        }

        val bitmap = suspendCoroutine<android.graphics.Bitmap> { continuation ->
            val action: (Result<android.graphics.Bitmap>) -> Unit = { continuation.resumeWith(it) }
            when (source) {
                MediaSource.GALLERY -> imagePickerFragment.pickGalleryImage(action)
                MediaSource.CAMERA -> imagePickerFragment.pickCameraImage(action)
            }
        }

        return Bitmap(bitmap)
    }

    override suspend fun pickMedia(): Media {
        val fragmentManager =
            fragmentManager ?: error("can't pick image without active window")

        permissionsController.providePermission(Permission.GALLERY)

        val currentFragment: Fragment? = fragmentManager.findFragmentByTag(mediaPickerFragmentTag)
        val pickerFragment: MediaPickerFragment = if (currentFragment != null) {
            currentFragment as MediaPickerFragment
        } else {
            MediaPickerFragment().apply {
                fragmentManager
                    .beginTransaction()
                    .add(this, mediaPickerFragmentTag)
                    .commitNow()
            }
        }

        return suspendCoroutine { continuation ->
            val action: (Result<Media>) -> Unit = { continuation.resumeWith(it) }
            pickerFragment.pickMedia(action)
        }
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
                    .add(this, filePickerFragmentTag)
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
}
