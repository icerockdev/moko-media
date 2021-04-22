/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.UIPresentationController
import platform.darwin.NSObject
import kotlin.coroutines.Continuation

internal actual class AdaptivePresentationDelegateToContinuation actual constructor(
    private val continuation: Continuation<*>
) : NSObject(), UIAdaptivePresentationControllerDelegateProtocol {
    override fun presentationControllerDidDismiss(presentationController: UIPresentationController) {
        continuation.resumeWith(Result.failure(CanceledException()))
    }
}