package com.icerockdev.library

import dev.icerock.moko.media.picker.MediaPickerController
import dev.icerock.moko.media.test.createBitmapMock
import dev.icerock.moko.media.test.createMediaPickerControllerMock
import dev.icerock.moko.mvvm.test.TestViewModelScope
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.test.createPermissionControllerMock
import dev.icerock.moko.test.AndroidArchitectureInstantTaskExecutorRule
import dev.icerock.moko.test.TestRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageSelectionViewModelTest {
    @get:TestRule
    val instantTaskExecutorRule = AndroidArchitectureInstantTaskExecutorRule()

    @BeforeTest
    fun setup() {
        TestViewModelScope.setupViewModelScope(CoroutineScope(Dispatchers.Unconfined))
    }

    @AfterTest
    fun tearDown() {
        TestViewModelScope.resetViewModelScope()
    }

    @Test
    fun `test successful bitmap`() {
        val cameraImage = createBitmapMock()
        val galleryImage = createBitmapMock()
        val permissionsController: PermissionsController = createPermissionControllerMock()
        val mediaPickerController: MediaPickerController = createMediaPickerControllerMock(
            permissionsController = permissionsController,
            cameraImageResult = cameraImage,
            galleryImageResult = galleryImage
        )
        val viewModel = ImageSelectionViewModel(
            mediaPickerController = mediaPickerController
        )

        viewModel.onGalleryPressed()

        assertEquals(expected = galleryImage, actual = viewModel.selectedImage.value)
    }
}
