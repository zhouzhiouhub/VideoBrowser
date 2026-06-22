package com.example.videobrowser.video

import androidx.media3.exoplayer.ExoPlayer
import com.example.videobrowser.utils.PlaybackSpeedNormalizer

internal class NativePlayerPlaybackSpeedController(
    private val playerProvider: () -> ExoPlayer?,
    private val gestureOverlay: () -> FullscreenVideoGestureOverlay?,
    private val saveDefaultVideoSpeed: (Float) -> Unit,
    private val speedNormalizer: (Float) -> Float = PlaybackSpeedNormalizer::normalize
) {
    private var selectedPlaybackSpeed = DEFAULT_PLAYBACK_SPEED

    fun currentSpeed(): Float {
        return selectedPlaybackSpeed
    }

    fun setSpeed(speed: Float) {
        updateSpeed(speed, persistDefault = true)
    }

    fun restoreSpeed(speed: Float) {
        updateSpeed(speed, persistDefault = false)
    }

    fun applyToPlayer(player: ExoPlayer) {
        player.setPlaybackSpeed(selectedPlaybackSpeed)
    }

    fun apply() {
        playerProvider()?.let(::applyToPlayer)
        gestureOverlay()?.setPlaybackSpeed(selectedPlaybackSpeed)
    }

    private fun updateSpeed(speed: Float, persistDefault: Boolean) {
        selectedPlaybackSpeed = speedNormalizer(speed)
        if (persistDefault) {
            saveDefaultVideoSpeed(selectedPlaybackSpeed)
        }
        apply()
    }

    private companion object {
        private const val DEFAULT_PLAYBACK_SPEED = 1f
    }
}
