/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import dev.icerock.moko.media.FileMedia
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.lastPathComponent
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.darwin.NSObject
import kotlin.coroutines.Continuation

internal class DocumentPickerDelegateToContinuation constructor(
    private val continuation: Continuation<FileMedia>
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        val info = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        if (info == null) {
            continuation.resumeWith(Result.failure(IllegalArgumentException("no file chooses")))
            return
        }
        val filename = info.lastPathComponent
        val path = info.absoluteString

        val url = NSURL.URLWithString(path.orEmpty())
            ?: throw IllegalArgumentException("invalid file path")
        val data =
            NSData.dataWithContentsOfURL(url) ?: throw IllegalArgumentException("invalid file data")
        val bytes = data.bytes ?: throw IllegalArgumentException("file bytes is null")

        val bytesPointer: CPointer<ByteVar> = bytes.reinterpret()
        val byteArray = ByteArray(data.length.toInt()) { index -> bytesPointer[index] }


        continuation.resumeWith(
            Result.success(
                FileMedia(
                    name = filename.orEmpty(),
                    path = path.orEmpty(),
                    byteArray = byteArray
                )
            )
        )
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        continuation.resumeWith(Result.failure(CanceledException()))
    }
}
