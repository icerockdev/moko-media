![moko-media](img/logo.png)  
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0) [![Download](https://img.shields.io/maven-central/v/dev.icerock.moko/media) ](https://repo1.maven.org/maven2/dev/icerock/moko/media) ![kotlin-version](https://kotlin-version.aws.icerock.dev/kotlin-version?group=dev.icerock.moko&name=media)

# Mobile Kotlin media access

This is a Kotlin MultiPlatform library that provides media picking in common code (photo/video) and
video player controls.

## Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Samples](#samples)
- [Set Up Locally](#set-up-locally)
- [Contributing](#contributing)
- [License](#license)

## Features

- Capture camera photo
- Pick image from gallery
- Control video player
- **Compose Multiplatform** support

## Requirements

- Gradle version 6.8+
- Android API 16+
- iOS version 11.0+

## Installation

root build.gradle

```groovy
allprojects {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

project build.gradle

```groovy
dependencies {
    commonMainApi("dev.icerock.moko:media:0.11.1")

    // Compose Multiplatform
    commonMainApi("dev.icerock.moko:media-compose:0.11.1")

    commonTestImplementation("dev.icerock.moko:media-test:0.11.1")
}
```

## Usage

```kotlin
class ViewModel(val mediaController: MediaPickerController) : ViewModel() {
    fun onSelectPhotoPressed() {
        launch {
            try {
                val bitmap = mediaController.pickImage(MediaControllerSource.CAMERA)
                // captured photo in bitmap
            } catch (_: CanceledException) {
                // cancel capture
            } catch (error: Throwable) {
                // denied permission or file read error
            }
        }
    }
}
```

android:

```kotlin
val viewModel = getViewModel {
    val permissionsController = PermissionsController()
    val mediaController = MediaPickerController(permissionsController)
    ViewModel(mediaController)
}

viewModel.mediaController.bind(
    lifecycle,
    supportFragmentManager
) // permissioncController bind automatically
```

iOS:

```swift
let permissionsController = PermissionsController()
let mediaController = MediaPickerController(permissionsController: permissionsController, viewController: self)
let viewModel = ViewModel(mediaController: mediaController)
```

### Compose Multiplatform

```kotlin
@Composable
fun Sample() {
    val factory = rememberMediaPickerControllerFactory()
    val picker = remember(factory) { factory.createMediaPickerController() }
    val coroutineScope = rememberCoroutineScope()

    BindMediaPickerEffect(picker)

    var image: ImageBitmap? by remember { mutableStateOf(null) }

    image?.let {
        Image(bitmap = it, contentDescription = null)
    }

    Button(
        onClick = {
            coroutineScope.launch {
                val result = picker.pickImage(MediaSource.GALLERY)
                image = result.toImageBitmap()
            }
        }
    ) {
        Text(text = "Click on me")
    }
}
```

or with moko-mvvm (with correct configuration change on android):

```kotlin
@Composable
fun Sample() {
    val factory = rememberMediaPickerControllerFactory()
    val viewModel: SampleViewModel = getViewModel(
        key = "sample",
        factory = viewModelFactory {
            val picker = factory.createMediaPickerController()
            SampleViewModel(picker)
        }
    )

    BindMediaPickerEffect(viewModel.mediaPickerController)

    val image: Bitmap? by viewModel.image.collectAsState()
    val imageBitmap: ImageBitmap? = remember(image) { image?.toImageBitmap() }

    imageBitmap?.let {
        Image(bitmap = it, contentDescription = null)
    }

    Button(onClick = viewModel::onButtonClick) {
        Text(text = "Click on me")
    }
}

class SampleViewModel(
    val mediaPickerController: MediaPickerController
) : ViewModel() {
    private val _image: MutableStateFlow<Bitmap?> = MutableStateFlow(null)
    val image: StateFlow<Bitmap?> get() = _image

    fun onButtonClick() {
        viewModelScope.launch {
            try {
                _image.value = mediaPickerController.pickImage(MediaSource.GALLERY)
            } catch (exc: Exception) {
                println("error $exc")
            }
        }
    }
}
```

## Samples

More examples can be found in the [sample directory](sample).

## Set Up Locally

- In [media directory](media) contains `media` library;
- In [sample directory](sample) contains samples on android, ios & mpp-library connected to apps.

## Contributing

All development (both new features and bug fixes) is performed in `develop` branch. This
way `master` sources always contain sources of the most recently released version. Please send PRs
with bug fixes to `develop` branch. Fixes to documentation in markdown files are an exception to
this rule. They are updated directly in `master`.

The `develop` branch is pushed to `master` during release.

More detailed guide for contributers see in [contributing guide](CONTRIBUTING.md).

## License

    Copyright 2019 IceRock MAG Inc
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
