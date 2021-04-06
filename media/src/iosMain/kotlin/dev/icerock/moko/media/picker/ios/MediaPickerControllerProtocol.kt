/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker.ios

import dev.icerock.moko.media.Bitmap
import dev.icerock.moko.media.FileMedia
import dev.icerock.moko.media.Media
import dev.icerock.moko.media.picker.MediaSource
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import platform.CoreServices.kUTTypeImage
import platform.CoreServices.kUTTypeMovie
import platform.CoreServices.kUTTypeVideo
import platform.Foundation.CFBridgingRelease
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UIViewController
import kotlin.coroutines.suspendCoroutine

interface MediaPickerControllerProtocol {
    val permissionsController: PermissionsController

    suspend fun pickImage(source: MediaSource): Bitmap
    suspend fun pickImage(source: MediaSource, maxWidth: Int, maxHeight: Int): Bitmap
    suspend fun pickMedia(): Media
    suspend fun pickFiles(): FileMedia
}
