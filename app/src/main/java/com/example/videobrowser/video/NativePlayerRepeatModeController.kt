package com.example.videobrowser.video

import androidx.media3.common.Player

internal class NativePlayerRepeatModeController(
    private val playerProvider: () -> Player?,
    private val gestureOverlay: () -> FullscreenVideoGestureOverlay?
) {
    private var repeatMode = PlaybackRepeatMode.NONE

    fun currentMode(): PlaybackRepeatMode {
        return repeatMode
    }

    fun setMode(mode: PlaybackRepeatMode) {
        repeatMode = mode
        apply()
    }

    fun cycle(queue: PlaybackQueue): PlaybackQueue {
        setMode(repeatMode.next())
        return applyToQueue(queue)
    }

    fun applyToQueue(queue: PlaybackQueue): PlaybackQueue {
        return queue.copy(repeatMode = repeatMode)
    }

    fun applyToPlayer(player: Player) {
        player.repeatMode = PlaybackRepeatModeMedia3Converter.toPlayerRepeatMode(repeatMode)
    }

    fun apply() {
        playerProvider()?.let(::applyToPlayer)
        gestureOverlay()?.setRepeatMode(repeatMode)
    }
}
