/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker.ios

import dev.icerock.moko.media.Bitmap
import dev.icerock.moko.media.FileMedia
import dev.icerock.moko.media.Media
import dev.icerock.moko.media.picker.MediaSource
import dev.icerock.moko.permissions.PermissionsController
import platform.UIKit.UIViewController

interface MediaPickerControllerProtocol {
    val permissionsController: PermissionsController

    fun bind(viewController: UIViewController)

    suspend fun pickImage(source: MediaSource): Bitmap
    suspend fun pickImage(source: MediaSource, maxWidth: Int, maxHeight: Int): Bitmap
    suspend fun pickMedia(): Media
    suspend fun pickFiles(): FileMedia
}
