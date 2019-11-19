object Versions {
    object Android {
        const val compileSdk = 28
        const val targetSdk = 28
        const val minSdk = 21
    }

    const val kotlin = "1.3.50"

    object Plugins {
        const val kotlin = Versions.kotlin
    }

    object Libs {
        object Android {
            const val appCompat = "1.0.2"
            const val exifInterface = "1.0.0"
            const val mediaFilePicker = "1.8"
        }

        object MultiPlatform {
            const val coroutines = "1.3.0"
            const val mokoPermissions = "0.1.0"
            const val mokoMedia = "0.1.0"
        }
    }
}