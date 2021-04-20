/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import dev.icerock.moko.media.FileMedia
import platform.Foundation.NSURL
import platform.Foundation.lastPathComponent
import platform.darwin.NSObject
import kotlin.coroutines.Continuation
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController

internal actual class DocumentPickerDelegateToContinuation actual constructor(
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

        continuation.resumeWith(Result.success(FileMedia(name = filename.orEmpty(), path = path.orEmpty())))
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        continuation.resumeWith(Result.failure(CanceledException()))
    }
}