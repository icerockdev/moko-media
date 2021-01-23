/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.player

import android.media.MediaPlayer
import android.net.Uri
import android.view.SurfaceHolder
import android.view.SurfaceView

actual class MediaPlayerController {
    private var mediaPlayer: MediaPlayer? = null
    private var surfaceHolder: SurfaceHolder? = null
    private var uri: Uri? = null

    private var isSurfaceReady = false
        set(value) {
            field = value
            if (value.and(isMediaPlayerReady)) onReady()
        }
    private var isMediaPlayerReady = false
        set(value) {
            field = value
            if (value.and(isSurfaceReady)) onReady()
        }

    private val mediaPlayerPreparedListener = MediaPlayer.OnPreparedListener {
        isMediaPlayerReady = true
    }
    private val surfaceViewCallback = object : SurfaceHolder.Callback {
        override fun surfaceChanged(holder: SurfaceHolder?, p1: Int, p2: Int, p3: Int) = Unit

        override fun surfaceDestroyed(holder: SurfaceHolder?) {
            isSurfaceReady = false
            this@MediaPlayerController.surfaceHolder = null
        }

        override fun surfaceCreated(holder: SurfaceHolder?) {
            holder?.let { surfaceHolder ->
                this@MediaPlayerController.surfaceHolder = surfaceHolder
                isSurfaceReady = true
            }
        }
    }

    private lateinit var listener: MediaPlayerListener

    private fun onReady() {
        mediaPlayer?.setDisplay(surfaceHolder)
        listener.onReady()
    }

    fun setSurfaceView(surfaceView: SurfaceView) {
        mediaPlayer?.run {
            setDataSource(surfaceView.context, uri!!) // FIXME MEDIUM cleanup
            prepareAsync()
        }
        surfaceView.holder.addCallback(surfaceViewCallback)
    }

    actual fun prepare(pathSource: String, listener: MediaPlayerListener) {
        this.listener = listener
        this.uri = Uri.parse(pathSource)
        mediaPlayer = MediaPlayer().apply {
            setOnCompletionListener { this@MediaPlayerController.listener.onVideoCompleted() }
            setOnPreparedListener(mediaPlayerPreparedListener)
        }
    }

    actual fun start() {
        mediaPlayer?.start()
    }

    actual fun pause() {
        mediaPlayer?.also { mediaPlayer ->
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            }
        }
    }

    actual fun stop() {
        mediaPlayer?.stop()
    }

    actual fun release() {
        isMediaPlayerReady = false
        mediaPlayer?.release()
        mediaPlayer = null
    }

    actual fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }
}
