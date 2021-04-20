package dev.icerock.moko.media.picker

import dev.icerock.moko.media.FileMedia
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.darwin.NSObject
import kotlin.coroutines.Continuation

internal expect class DocumentPickerDelegateToContinuation(
    continuation: Continuation<FileMedia>
) : NSObject, UIDocumentPickerDelegateProtocol
