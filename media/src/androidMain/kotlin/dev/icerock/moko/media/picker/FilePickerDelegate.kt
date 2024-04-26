package dev.icerock.moko.media.picker

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContracts
import dev.icerock.moko.media.FileMedia
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

internal class FilePickerDelegate {

    private var callback: CallbackData? = null

    private val filePickerLauncherHolder =
        MutableStateFlow<ActivityResultLauncher<Array<String>>?>(null)

    fun bind(activity: ComponentActivity) {
        val activityResultRegistryOwner = activity as ActivityResultRegistryOwner
        val activityResultRegistry = activityResultRegistryOwner.activityResultRegistry

        filePickerLauncherHolder.value = activityResultRegistry.register(
            PICK_FILE_KEY,
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            val callbackData = callback ?: return@register
            callback = null

            val callback = callbackData.callback

            if (uri == null) {
                callback.invoke(Result.failure(CanceledException()))
                return@register
            }

            if (uri.path == null) {
                callback.invoke(Result.failure(java.lang.IllegalStateException("File is null")))
                return@register
            }

            @SuppressLint("Range")
            val fileNameWithExtension = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                val cursor = activity.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (!it.moveToFirst()) null
                    else it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } else null

            uri.path?.let { path ->
                val file = File(path)
                val name = file.name
                callback(
                    Result.success(
                        FileMedia(
                            fileNameWithExtension ?: name,
                            uri.toString(),
                        )
                    )
                )
            }
        }
    }

    fun pickFile(callback: (Result<FileMedia>) -> Unit) {
        this.callback?.let {
            it.callback.invoke(Result.failure(IllegalStateException("Callback should be null")))
            this.callback = null
        }

        this.callback = CallbackData(callback)

        filePickerLauncherHolder.value?.launch(
            arrayOf(
                "*/*",
            )
        )
    }

    class CallbackData(val callback: (Result<FileMedia>) -> Unit)

    companion object {
        private const val PICK_FILE_KEY = "PickFileKey"
    }
}
