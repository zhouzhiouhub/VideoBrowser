package com.example.videobrowser.video

import android.util.Log
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView

internal class NativePlayerControlsVisibilityController(
    private val playerView: PlayerView,
    private val playerProvider: () -> Player?,
    private val logTag: String
) {
    fun applyDefaultHideTimeout() {
        playerView.setControllerShowTimeoutMs(CONTROLS_HIDE_DELAY_MS)
    }

    fun wakeControls() {
        val player = playerProvider()
        val keepVisible = shouldKeepControlsVisible(player)
        Log.d(
            logTag,
            "event=native-wake-controls keepVisible=$keepVisible " +
                "state=${player?.playbackState} playWhenReady=${player?.playWhenReady}"
        )
        playerView.setControllerShowTimeoutMs(
            if (keepVisible) {
                0
            } else {
                CONTROLS_HIDE_DELAY_MS
            }
        )
        playerView.showController()
    }

    fun areControlsVisible(): Boolean {
        return playerView.isControllerFullyVisible
    }

    private fun shouldKeepControlsVisible(player: Player?): Boolean {
        val exoPlayer = player ?: return false
        return !exoPlayer.playWhenReady ||
            exoPlayer.playbackState == Player.STATE_IDLE ||
            exoPlayer.playbackState == Player.STATE_ENDED
    }

    companion object {
        const val CONTROLS_HIDE_DELAY_MS = 3000
    }
}
