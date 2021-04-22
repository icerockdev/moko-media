/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker.ios

import dev.icerock.moko.media.Bitmap
import dev.icerock.moko.media.FileMedia
import dev.icerock.moko.media.Media
import dev.icerock.moko.media.picker.AdaptivePresentationDelegateToContinuation
import dev.icerock.moko.media.picker.DEFAULT_MAX_IMAGE_HEIGHT
import dev.icerock.moko.media.picker.DEFAULT_MAX_IMAGE_WIDTH
import dev.icerock.moko.media.picker.DocumentPickerDelegateToContinuation
import dev.icerock.moko.media.picker.ImagePickerDelegateToContinuation
import dev.icerock.moko.media.picker.MediaSource
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import platform.CoreServices.kUTTypeImage
import platform.CoreServices.kUTTypeMovie
import platform.CoreServices.kUTTypeVideo
import platform.Foundation.CFBridgingRelease
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UIViewController
import kotlin.coroutines.suspendCoroutine
import platform.CoreServices.kUTTypeData
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.presentationController

class MediaPickerController(
    override val permissionsController: PermissionsController,
    private val getViewController: () -> UIViewController
) : MediaPickerControllerProtocol {
    constructor(
        permissionsController: PermissionsController,
        viewController: UIViewController
    ) : this(
        permissionsController = permissionsController,
        getViewController = { viewController }
    )

    override suspend fun pickImage(source: MediaSource): Bitmap {
        return pickImage(source, DEFAULT_MAX_IMAGE_WIDTH, DEFAULT_MAX_IMAGE_HEIGHT)
    }

    override suspend fun pickImage(source: MediaSource, maxWidth: Int, maxHeight: Int): Bitmap {
        source.requiredPermissions().forEach { permission ->
            permissionsController.providePermission(permission)
        }

        @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
        var delegatePtr: ImagePickerDelegateToContinuation? // strong reference to delegate (view controller have weak ref)
        var presentationDelegate: AdaptivePresentationDelegateToContinuation?
        val media = suspendCoroutine<Media> { continuation ->
            val localDelegatePtr = ImagePickerDelegateToContinuation(continuation)
            delegatePtr = localDelegatePtr
            val localPresentationDelegatePtr = AdaptivePresentationDelegateToContinuation(continuation)
            presentationDelegate = localPresentationDelegatePtr

            val controller = UIImagePickerController()
            controller.sourceType = source.toSourceType()
            controller.mediaTypes = listOf(kImageType)
            controller.delegate = localDelegatePtr
            getViewController().presentViewController(
                controller,
                animated = true,
                completion = null
            )
            controller.presentationController?.delegate = localPresentationDelegatePtr
        }
        delegatePtr = null
        presentationDelegate = null
        return media.preview
    }

    override suspend fun pickFiles(): FileMedia {
        var delegatePtr: DocumentPickerDelegateToContinuation? // strong reference to delegate (view controller have weak ref)
        var presentationDelegate: AdaptivePresentationDelegateToContinuation?
        val fileMedia = suspendCoroutine<FileMedia> { continuation ->
            val localDelegatePtr = DocumentPickerDelegateToContinuation(continuation)
            delegatePtr = localDelegatePtr
            val localPresentationDelegatePtr = AdaptivePresentationDelegateToContinuation(continuation)
            presentationDelegate = localPresentationDelegatePtr

            val controller = UIDocumentPickerViewController(
                    documentTypes = listOf(kStandardFileTypesId),
                    inMode = UIDocumentPickerMode.UIDocumentPickerModeOpen
            )
            controller.delegate = localDelegatePtr
            getViewController().presentViewController(
                    controller,
                    animated = true,
                    completion = null
            )
            controller.presentationController?.delegate = localPresentationDelegatePtr
        }
        delegatePtr = null
        presentationDelegate = null
        return fileMedia
    }

    private fun MediaSource.requiredPermissions(): List<Permission> =
        when (this) {
            MediaSource.GALLERY -> listOf(Permission.GALLERY)
            MediaSource.CAMERA -> listOf(Permission.CAMERA)
        }

    private fun MediaSource.toSourceType(): UIImagePickerControllerSourceType =
        when (this) {
            MediaSource.GALLERY -> UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            MediaSource.CAMERA -> UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        }

    override suspend fun pickMedia(): Media {
        permissionsController.providePermission(Permission.GALLERY)

        @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
        var delegatePtr: ImagePickerDelegateToContinuation? // strong reference to delegate (view controller have weak ref)
        val media = suspendCoroutine<Media> { continuation ->
            val localDelegatePtr = ImagePickerDelegateToContinuation(continuation)
            delegatePtr = localDelegatePtr

            val controller = UIImagePickerController()
            controller.sourceType =
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            controller.mediaTypes = listOf(kImageType, kVideoType, kMovieType)
            controller.delegate = localDelegatePtr
            getViewController().presentViewController(
                controller,
                animated = true,
                completion = null
            )
        }
        delegatePtr = null

        return media
    }

    internal companion object {
        val kVideoType = CFBridgingRelease(kUTTypeVideo) as String
        val kMovieType = CFBridgingRelease(kUTTypeMovie) as String
        val kImageType = CFBridgingRelease(kUTTypeImage) as String
        val kStandardFileTypesId = CFBridgingRelease(kUTTypeData) as String
    }
}
