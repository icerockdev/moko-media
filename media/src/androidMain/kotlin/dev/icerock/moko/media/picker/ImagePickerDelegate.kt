package dev.icerock.moko.media.picker

import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dev.icerock.moko.media.BitmapUtils
import kotlinx.coroutines.flow.MutableStateFlow

internal class ImagePickerDelegate {

    private var callback: CallbackData? = null

    private val takePictureLauncherHolder = MutableStateFlow<ActivityResultLauncher<Uri>?>(null)
    private val pickVisualMediaLauncherHolder =
        MutableStateFlow<ActivityResultLauncher<PickVisualMediaRequest>?>(null)

    fun bind(activity: ComponentActivity) {
        val activityResultRegistryOwner = activity as ActivityResultRegistryOwner
        val activityResultRegistry = activityResultRegistryOwner.activityResultRegistry

        pickVisualMediaLauncherHolder.value = activityResultRegistry.register(
            PICK_GALLERY_IMAGE_KEY,
            ActivityResultContracts.PickVisualMedia(),
        ) { uri ->
            val callbackData = callback ?: return@register
            callback = null

            val callback = callbackData.callback

            if (uri == null) {
                callback.invoke(Result.failure(CanceledException()))
                return@register
            }

            processResult(activity, callback, uri)
        }

        takePictureLauncherHolder.value = activityResultRegistry.register(
            "TakePicture",
            ActivityResultContracts.TakePicture(),
        ) { result ->
            val callbackData = callback ?: return@register
            callback = null

            if (callbackData !is CallbackData.Camera) {
                callbackData.callback.invoke(
                    Result.failure(
                        IllegalStateException("Callback type should be Camera")
                    )
                )
                return@register
            }

            if (!result) {
                callbackData.callback.invoke(Result.failure(CanceledException()))
                return@register
            }

            processResult(activity, callbackData.callback, callbackData.outputUri)
        }

        val observer = object : LifecycleEventObserver {

            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    takePictureLauncherHolder.value = null
                    pickVisualMediaLauncherHolder.value = null
                    source.lifecycle.removeObserver(this)
                }
            }
        }
        activity.lifecycle.addObserver(observer)
    }

    fun pickGalleryImage(
        maxWidth: Int,
        maxHeight: Int,
        callback: (Result<android.graphics.Bitmap>) -> Unit,
    ) {
        this.callback?.let {
            it.callback.invoke(Result.failure(IllegalStateException("Callback should be null")))
            this.callback = null
        }

        this.callback = CallbackData.Gallery(callback)

        pickVisualMediaLauncherHolder.value?.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    fun pickCameraImage(
        maxWidth: Int,
        maxHeight: Int,
        callback: (Result<android.graphics.Bitmap>) -> Unit,
        outputUri: Uri,
    ) {
        this.callback?.let {
            it.callback.invoke(Result.failure(IllegalStateException("Callback should be null")))
            this.callback = null
        }

        this.callback = CallbackData.Camera(callback, outputUri)

        takePictureLauncherHolder.value?.launch(
            outputUri
        )
    }

    @Suppress("ReturnCount")
    private fun processResult(
        context: Context,
        callback: (Result<android.graphics.Bitmap>) -> Unit,
        uri: Uri,
        maxImageWidth: Int = DEFAULT_MAX_IMAGE_WIDTH,
        maxImageHeight: Int = DEFAULT_MAX_IMAGE_HEIGHT,
    ) {
        val contentResolver = context.contentResolver

        val bitmapOptions = contentResolver.openInputStream(uri)?.use {
            BitmapUtils.getBitmapOptionsFromStream(it)
        } ?: run {
            callback.invoke(Result.failure(NoAccessToFileException(uri.toString())))
            return
        }

        val sampleSize =
            BitmapUtils.calculateInSampleSize(bitmapOptions, maxImageWidth, maxImageHeight)

        val orientation = contentResolver.openInputStream(uri)?.use {
            BitmapUtils.getBitmapOrientation(it)
        } ?: run {
            callback.invoke(Result.failure(NoAccessToFileException(uri.toString())))
            return
        }

        val bitmap = contentResolver.openInputStream(uri)?.use {
            BitmapUtils.getNormalizedBitmap(it, orientation, sampleSize)
        } ?: run {
            callback.invoke(Result.failure(NoAccessToFileException(uri.toString())))
            return
        }

        callback.invoke(Result.success(bitmap))
    }

    sealed class CallbackData(val callback: (Result<android.graphics.Bitmap>) -> Unit) {
        class Gallery(callback: (Result<android.graphics.Bitmap>) -> Unit) :
            CallbackData(callback)

        class Camera(
            callback: (Result<android.graphics.Bitmap>) -> Unit,
            val outputUri: Uri
        ) : CallbackData(callback)
    }

    companion object {
        private const val PICK_GALLERY_IMAGE_KEY = "PickGalleryImageKey"
    }
}
