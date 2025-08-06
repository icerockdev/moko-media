/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import dev.icerock.moko.media.Bitmap
import dev.icerock.moko.media.Media
import dev.icerock.moko.media.MediaType
import dev.icerock.moko.media.picker.ios.MediaPickerController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVAsset
import platform.AVFoundation.AVAssetImageGenerator
import platform.AVFoundation.AVURLAsset
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSURL
import platform.UIKit.UIImage
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerEditedImage
import platform.UIKit.UIImagePickerControllerImageURL
import platform.UIKit.UIImagePickerControllerMediaType
import platform.UIKit.UIImagePickerControllerMediaURL
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import kotlin.coroutines.Continuation
import kotlin.random.Random

@OptIn(ExperimentalForeignApi::class)
internal class ImagePickerDelegateToContinuation constructor(
    private val continuation: Continuation<Media>
) : NSObject(), UINavigationControllerDelegateProtocol, UIImagePickerControllerDelegateProtocol {

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissModalViewControllerAnimated(true)
        continuation.resumeWith(Result.failure(CanceledException()))
    }

    @Suppress("ReturnCount")
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

        val type = when (mediaType) {
            MediaPickerController.kMovieType, MediaPickerController.kVideoType -> MediaType.VIDEO
            MediaPickerController.kImageType -> MediaType.PHOTO
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
            if (image == null) {
                continuation.resumeWith(Result.failure(NoAccessToFileException("info: $info"))) // TODO write some info
                return
            }

            val media = Media(
                name = mediaUrl?.relativeString ?: Random.nextLong().toString(),
                path = mediaUrl?.path.orEmpty(),
                preview = Bitmap(image),
                type = type
            )
            continuation.resumeWith(Result.success(media))
        }
    }

    // Стоит сделать асинхронно и придумать что делать с ошибкой
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
