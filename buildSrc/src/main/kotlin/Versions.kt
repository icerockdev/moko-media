object Versions {
    object Android {
        const val compileSdk = 28
        const val targetSdk = 28
        const val minSdk = 16
    }

    const val kotlin = "1.3.60"

    object Plugins {
        const val kotlin = Versions.kotlin
    }

    object Libs {
        object Android {
            const val appCompat = "1.1.0"
            const val exifInterface = "1.0.0"
            const val mediaFilePicker = "1.8"
        }

        object MultiPlatform {
            const val coroutines = "1.3.2-1.3.60-eap-76"
            const val mokoPermissions = "0.3.0-dev-1"
            const val mokoMedia = "0.2.0-dev-1"
        }
    }
}