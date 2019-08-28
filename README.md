[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[ ![Download](https://api.bintray.com/packages/icerockdev/moko/moko-media/images/download.svg) ](https://bintray.com/icerockdev/moko/moko-media/_latestVersion)

# Базовые компоненты для мультиплатформы
## Media picker
Мультиплатформенный выбор медиа позволяет с уровня `viewModel` инициировать выбор фото/видео, с последующим
 получением нужных пермиссий и выдачей результата в виде `coroutine`. Выглядит это так:
```kotlin
class ViewModel(val mediaController: MediaController): ViewModel() {
    fun onSelectPhotoPressed() {
        launch {
            try {
                val bitmap = mediaController.pickImage(MediaControllerSource.CAMERA)
                // в bitmap нахоидтся снятое на камеру пользователем фото
            } catch(_: CanceledException) {
                // если пользователь отменил выбор фото
            } catch(error: Throwable) {
                // ошибка может быть платформенная (отказ от выдачи разрешения, отказ навсегда)
            }
        }
    }
}
```

Создается контроллер следующим образом:  
android:
```kotlin
val viewModel = getViewModel {
    val permissionsController = PermissionsController()
    val mediaController = MediaController(permissionsController)
    ViewModel(mediaController)
}

viewModel.mediaController.bind(lifecycle, supportFragmentManager) // биндит автоматом и permissionsController тоже, поэтому отдельно пермиссии привязывать не надо
```
iOS:
```swift
let permissionsController = PermissionsController()
let mediaController = MediaController(permissionsController: permissionsController, viewController: self)
let viewModel = ViewModel(mediaController: mediaController)
```

Результат выбора фото является классом `Bitmap` - это мультиплатформенное представление изображения,
 которое на платформах является `Bitmap`/`UIImage` (android/ios соответственно). В общем коде позволяет
 получить `ByteArray` и `Base64 string`.
