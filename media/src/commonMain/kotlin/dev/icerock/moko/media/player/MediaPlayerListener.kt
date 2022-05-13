/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.player

interface MediaPlayerListener {
    fun onReady()
    fun onVideoCompleted()
    // TODO HIGH add onPlay, onPause events
}
