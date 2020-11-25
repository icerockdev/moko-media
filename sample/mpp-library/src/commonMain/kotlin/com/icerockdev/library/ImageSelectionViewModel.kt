package com.icerockdev.library

import dev.icerock.moko.media.Bitmap
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

    fun onSelectImagePressed() {
        viewModelScope.launch {
            try {
                val image = mediaPickerController.pickImage(MediaSource.CAMERA)
                _selectedImage.value = image
                _textState.value = "image selected"
            } catch (exc: Exception) {
                exc.printStackTrace()
                _selectedImage.value = null
                _textState.value = exc.toString()
            }
        }
    }
}
