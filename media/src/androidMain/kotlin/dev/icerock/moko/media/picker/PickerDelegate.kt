/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.picker

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @param <C> type of callback
 * @param <I> type of the input required to launch
 */
internal abstract class PickerDelegate<C, I> {

    protected var callback: C? = null

    protected val pickerLauncherHolder =
        MutableStateFlow<ActivityResultLauncher<I>?>(null)

    fun bind(activity: ComponentActivity) {
        val activityResultRegistryOwner = activity as ActivityResultRegistryOwner
        val activityResultRegistry = activityResultRegistryOwner.activityResultRegistry

        pickerLauncherHolder.value = registerActivityResult(
            context = activity,
            activityResultRegistry = activityResultRegistry,
        )

        val observer = object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    pickerLauncherHolder.value = null
                    source.lifecycle.removeObserver(this)
                }
            }
        }
        activity.lifecycle.addObserver(observer)
    }

    abstract fun registerActivityResult(
        context: Context,
        activityResultRegistry: ActivityResultRegistry,
    ): ActivityResultLauncher<I>
}
