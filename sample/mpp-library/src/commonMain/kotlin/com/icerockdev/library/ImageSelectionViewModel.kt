package com.icerockdev.library

import dev.icerock.moko.media.Bitmap
import dev.icerock.moko.media.picker.CanceledException
import dev.icerock.moko.media.picker.MediaPickerController
import dev.icerock.moko.media.picker.MediaSource
import dev.icerock.moko.mvvm.livedata.LiveData
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import dev.icerock.moko.mvvm.livedata.readOnly
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.launch

class ImageSelectionViewModel(
    val mediaPickerController: MediaPickerController
) : ViewModel() {
    private val _selectedImage = MutableLiveData<Bitmap?>(initialValue = null)
    val selectedImage: LiveData<Bitmap?> = _selectedImage.readOnly()

    private val _textState = MutableLiveData(initialValue = "wait actions...")
    val textState: LiveData<String> = _textState.readOnly()

    fun onCameraPressed() {
        selectFile()
    }

    fun onGalleryPressed() {
        selectImage(MediaSource.GALLERY)
    }

    @Suppress("TooGenericExceptionCaught")
    fun selectFile() {
        viewModelScope.launch {
            @Suppress("SwallowedException")
            try {
                val file = mediaPickerController.pickFiles()
                _textState.value = file.name
            } catch (canceled: CanceledException) {
                _textState.value = "canceled"
            } catch (exc: Exception) {
                _textState.value = exc.toString()
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun selectImage(source: MediaSource) {
        viewModelScope.launch {
            @Suppress("SwallowedException")
            try {
                val image = mediaPickerController.pickImage(source)
                _selectedImage.value = image
                _textState.value = "image selected"
            } catch (canceled: CanceledException) {
                _textState.value = "canceled"
            } catch (exc: Exception) {
                exc.printStackTrace()
                _selectedImage.value = null
                _textState.value = exc.toString()
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun selectMedia() {
        viewModelScope.launch {
            @Suppress("SwallowedException")
            try {
                val image = mediaPickerController.pickMedia()
                _textState.value = image.name
                _selectedImage.value = image.preview
            } catch (canceled: CanceledException) {
                _textState.value = "canceled"
            } catch (exc: Exception) {
                exc.printStackTrace()
                _textState.value = exc.toString()
            }
        }
    }
}
