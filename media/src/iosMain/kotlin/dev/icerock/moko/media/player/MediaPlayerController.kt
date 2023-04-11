/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.media.player

import kotlinx.cinterop.cValue
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerTimeControlStatusPlaying
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.seekToTime
import platform.AVFoundation.timeControlStatus
import platform.AVKit.AVPlayerViewController
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.darwin.NSObjectProtocol
import kotlin.native.ref.WeakReference

actual class MediaPlayerController {

    private var player: AVPlayer? = null
        set(value) {
            field = value
            checkReady()
        }
    private var listener: WeakReference<MediaPlayerListener>? = null
    private var observer: NSObjectProtocol? = null

    var playerController: AVPlayerViewController? = null
        set(value) {
            field = value
            checkReady()
        }

    private fun checkReady() {
        if (player != null && playerController != null) onReady()
    }

    actual fun prepare(pathSource: String, listener: MediaPlayerListener) {
        this.listener = WeakReference(listener)

        val url = NSURL(string = pathSource)
        this.player = AVPlayer(uRL = url)

        observer = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
            usingBlock = {
                listener.onVideoCompleted()
            }
        )
    }

    private fun onReady() {
        val playerController = playerController ?: return
        val player = player ?: return

        playerController.player = player

        listener?.get()?.onReady()
    }

    actual fun start() {
        player?.play()
    }

    actual fun pause() {
        player?.pause()
    }

    actual fun stop() {
        player?.run {
            pause()
            seekToTime(time = cValue {
                value = 0
            })
        }
    }

    actual fun isPlaying(): Boolean {
        return player?.timeControlStatus == AVPlayerTimeControlStatusPlaying
    }

    actual fun release() {
        observer?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
    }
}
