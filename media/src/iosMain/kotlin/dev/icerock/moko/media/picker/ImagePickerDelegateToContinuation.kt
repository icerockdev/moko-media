/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import dev.icerock.moko.media.Media
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import kotlin.coroutines.Continuation

internal expect class ImagePickerDelegateToContinuation(
    continuation: Continuation<Media>
) : NSObject, UINavigationControllerDelegateProtocol, UIImagePickerControllerDelegateProtocol
