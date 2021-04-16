![moko-media](img/logo.png)  
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0) [![Download](https://img.shields.io/maven-central/v/dev.icerock.moko/media) ](https://repo1.maven.org/maven2/dev/icerock/moko/media) ![kotlin-version](https://img.shields.io/badge/kotlin-1.4.31-orange)

# Mobile Kotlin media access
This is a Kotlin MultiPlatform library that provides media picking in common code (photo/video) and video player controls. 

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
TODO

## Requirements
- Gradle version 6.0+
- Android API 16+
- iOS version 9.0+

## Versions
### Bintray
- kotlin 1.3.50
  - 0.1.0
- kotlin 1.3.61
  - 0.2.0
  - 0.3.0
- kotlin 1.3.70
  - 0.4.0
  - 0.4.1
  - 0.4.2
  - 0.4.3
- kotlin 1.4.0
  - 0.5.0
  - 0.6.0
- kotlin 1.4.21
  - 0.6.1
### mavenCentral
- kotlin 1.4.31
  - 0.6.2
  - 0.7.0
  - 0.7.1

## Installation
root build.gradle  
```groovy
allprojects {
    repositories {
        mavenCentral()
        maven { url = "http://dl.bintray.com/lukaville/maven" }
    }
}
```

project build.gradle
```groovy
dependencies {
    commonMainApi("dev.icerock.moko:media:0.7.1")
}
```

## Usage
TODO

```kotlin
class ViewModel(val mediaController: MediaPickerController): ViewModel() {
    fun onSelectPhotoPressed() {
        launch {
            try {
                val bitmap = mediaController.pickImage(MediaControllerSource.CAMERA)
                // captured photo in bitmap
            } catch(_: CanceledException) {
                // cancel capture
            } catch(error: Throwable) {
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

viewModel.mediaController.bind(lifecycle, supportFragmentManager) // permissioncController bind automatically
```
iOS:
```swift
let permissionsController = PermissionsController()
let mediaController = MediaPickerController(permissionsController: permissionsController, viewController: self)
let viewModel = ViewModel(mediaController: mediaController)
```

## Samples
More examples can be found in the [sample directory](sample).

## Set Up Locally 
- In [media directory](media) contains `media` library;
- In [sample directory](sample) contains samples on android, ios & mpp-library connected to apps.

## Contributing
All development (both new features and bug fixes) is performed in `develop` branch. This way `master` sources always contain sources of the most recently released version. Please send PRs with bug fixes to `develop` branch. Fixes to documentation in markdown files are an exception to this rule. They are updated directly in `master`.

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
