package com.icerockdev.library

import dev.icerock.moko.media.Bitmap
import dev.icerock.moko.media.picker.MediaPickerController
import dev.icerock.moko.media.picker.MediaSource
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import dev.icerock.moko.permissions.DeniedAlwaysException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ComposeAppSampleViewModel(
    val mediaPickerController: MediaPickerController
): ViewModel() {
    private val _image: MutableStateFlow<Bitmap?> = MutableStateFlow(null)
    val image: StateFlow<Bitmap?> get() = _image

    fun onGalleryButtonClick() {
        pickImage(MediaSource.GALLERY)
    }

    fun onCameraButtonClick() {
        pickImage(MediaSource.CAMERA)
    }

    private fun pickImage(mediaSource: MediaSource) {
        viewModelScope.launch {
            try {
                _image.value = mediaPickerController.pickImage(mediaSource)
            } catch (_: DeniedAlwaysException) {
                println("route to permissions settings")
            } catch (exc: Exception) {
                println("show error alert $exc")
            }
        }
    }
}
