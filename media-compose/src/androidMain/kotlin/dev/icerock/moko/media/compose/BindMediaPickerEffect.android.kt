/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import dev.icerock.moko.media.picker.MediaPickerController

@Suppress("FunctionNaming")
@Composable
actual fun BindMediaPickerEffect(mediaPickerController: MediaPickerController) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val context: Context = LocalContext.current

    LaunchedEffect(mediaPickerController, lifecycleOwner, context) {
        val activity: FragmentActivity = checkNotNull(context as? FragmentActivity) {
            "$context context is not instance of FragmentActivity"
        }

        mediaPickerController.bind(activity)
    }
}
