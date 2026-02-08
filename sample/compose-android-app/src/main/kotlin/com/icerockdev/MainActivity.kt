/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.icerockdev.library.ComposeAppSampleViewModel
import dev.icerock.moko.media.Bitmap
import dev.icerock.moko.media.compose.BindMediaPickerEffect
import dev.icerock.moko.media.compose.rememberMediaPickerControllerFactory
import dev.icerock.moko.media.compose.toImageBitmap
import dev.icerock.moko.mvvm.getViewModel

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.background(Color.White)) {
                    val factory = rememberMediaPickerControllerFactory()
                    val picker = remember(factory) { factory.createMediaPickerController() }

                    TestScreen(
                        viewModel = getViewModel {
                            ComposeAppSampleViewModel(
                                mediaPickerController = picker
                            )
                        }
                    )
                }
            }
        }
    }
}

@Suppress("FunctionNaming")
@Composable
fun TestScreen(viewModel: ComposeAppSampleViewModel) {
    BindMediaPickerEffect(viewModel.mediaPickerController)

    val image: Bitmap? by viewModel.image.collectAsState()
    val imageBitmap: ImageBitmap? = remember(image) { image?.toImageBitmap() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        imageBitmap?.let {
            Image(bitmap = it, contentDescription = null)
        }

        Button(
            onClick = viewModel::onGalleryButtonClick
        ) {
            Text(text = "get from gallery")
        }

        Button(
            onClick = viewModel::onCameraButtonClick
        ) {
            Text(text = "get from camera")
        }
    }
}
