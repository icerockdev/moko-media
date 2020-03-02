/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import dev.icerock.moko.media.Bitmap
import dev.icerock.moko.media.FileMedia
import dev.icerock.moko.media.Media
import dev.icerock.moko.media.MediaType
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import platform.AVFoundation.AVAsset
import platform.AVFoundation.AVAssetImageGenerator
import platform.AVFoundation.AVURLAsset
import platform.CoreMedia.CMTimeMake
import platform.CoreServices.kUTTypeImage
import platform.CoreServices.kUTTypeMovie
import platform.CoreServices.kUTTypeVideo
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSURL
import platform.UIKit.UIImage
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerEditedImage
import platform.UIKit.UIImagePickerControllerImageURL
import platform.UIKit.UIImagePickerControllerMediaType
import platform.UIKit.UIImagePickerControllerMediaURL
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

actual class MediaPickerController(
    val permissionsController: PermissionsController,
    private val getViewController: () -> UIViewController
) {
    constructor(
        permissionsController: PermissionsController,
        viewController: UIViewController
    ) : this(
        permissionsController = permissionsController,
        getViewController = { viewController }
    )

    actual suspend fun pickImage(source: MediaSource): Bitmap {
        source.requiredPermissions().forEach { permission ->
            permissionsController.providePermission(permission)
        }

        var delegatePtr: Delegate? // strong reference to delegate (view controller have weak ref)
        val media = suspendCoroutine<Media> { continuation ->
            delegatePtr = Delegate(continuation)

            val controller = UIImagePickerController()
            controller.sourceType = source.toSourceType()
            controller.mediaTypes = listOf(kImageType)
            controller.delegate = delegatePtr
            getViewController().presentViewController(controller, animated = true, completion = null)
        }
        delegatePtr = null

        return media.preview
    }

    actual suspend fun pickFiles(): FileMedia {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    class Delegate(private val continuation: Continuation<Media>) : NSObject(),
        UINavigationControllerDelegateProtocol,
        UIImagePickerControllerDelegateProtocol {

        override fun imagePickerController(
            picker: UIImagePickerController,
            didFinishPickingMediaWithInfo: Map<Any?, *>
        ) {
            @Suppress("UnnecessaryVariable")
            val info = didFinishPickingMediaWithInfo

            val image = info[UIImagePickerControllerEditedImage] as? UIImage
                ?: info[UIImagePickerControllerOriginalImage] as? UIImage
            val mediaUrl = info[UIImagePickerControllerMediaURL] as? NSURL
                ?: info[UIImagePickerControllerImageURL] as? NSURL
            val mediaType = info[UIImagePickerControllerMediaType] as? String

            picker.dismissViewControllerAnimated(true) {}

            //TODO: Строковые константы могут отличаться в разных версиях. Хардкодить их нехорошо, CFBridgingRelease(kUTTypeVideo) возвращает неверный тип.
            val type = when (mediaType) {
                kMovieType, kVideoType -> MediaType.VIDEO
                kImageType -> MediaType.PHOTO
                else -> {
                    continuation.resumeWith(Result.failure(IllegalArgumentException("unknown type $mediaType")))
                    return
                }
            }

            if (type == MediaType.VIDEO) {
                if (mediaUrl == null) {
                    continuation.resumeWith(Result.failure(NoAccessToFileException("info: $info"))) // TODO write some info
                    return
                }

                val asset = AVURLAsset(uRL = mediaUrl, options = null)
                val media = Media(
                    name = mediaUrl.relativeString,
                    path = mediaUrl.path.orEmpty(),
                    preview = Bitmap(fetchThumbnail(videoAsset = asset)),
                    type = type
                )
                continuation.resumeWith(Result.success(media))
            } else {
                if (image == null || mediaUrl == null) {
                    continuation.resumeWith(Result.failure(NoAccessToFileException("info: $info"))) // TODO write some info
                    return
                }

                val media = Media(
                    name = mediaUrl.relativeString,
                    path = mediaUrl.path.orEmpty(),
                    preview = Bitmap(image),
                    type = type
                )
                continuation.resumeWith(Result.success(media))
            }
        }

        //TODO: Стоит сделать асинхронно и придумать что делать с ошибкой
        private fun fetchThumbnail(videoAsset: AVAsset): UIImage {
            val imageGenerator = AVAssetImageGenerator(
                asset = videoAsset
            )
            imageGenerator.appliesPreferredTrackTransform = true
            val cgImage = imageGenerator.copyCGImageAtTime(
                requestedTime = CMTimeMake(
                    value = 0,
                    timescale = 1
                ),
                actualTime = null,
                error = null
            )
            return UIImage(cGImage = cgImage)
        }
    }

    actual suspend fun pickMedia(): Media {
        permissionsController.providePermission(Permission.GALLERY)

        var delegatePtr: Delegate? // strong reference to delegate (view controller have weak ref)
        val media = suspendCoroutine<Media> { continuation ->
            delegatePtr = Delegate(continuation)

            val controller = UIImagePickerController()
            controller.sourceType =
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            controller.mediaTypes = listOf(kImageType, kVideoType, kMovieType)
            controller.delegate = delegatePtr
            getViewController().presentViewController(controller, animated = true, completion = null)
        }
        delegatePtr = null

        return media
    }

    private companion object {
        val kVideoType = CFBridgingRelease(kUTTypeVideo) as String
        val kMovieType = CFBridgingRelease(kUTTypeMovie) as String
        val kImageType = CFBridgingRelease(kUTTypeImage) as String
    }
}