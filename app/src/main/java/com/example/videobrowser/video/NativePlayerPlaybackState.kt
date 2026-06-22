package com.example.videobrowser.video

import androidx.media3.exoplayer.ExoPlayer

internal class NativePlayerPlaybackState {
    var playbackPosition = 0L
        private set
    var playWhenReady = true
        private set
    var currentMediaItemIndex = 0
        private set

    fun restoreFrom(savedState: NativePlayerSavedState) {
        playbackPosition = savedState.playbackPosition
        playWhenReady = savedState.playWhenReady
        currentMediaItemIndex = savedState.currentMediaItemIndex
    }

    fun setCurrentMediaItemIndex(index: Int) {
        currentMediaItemIndex = index
    }

    fun setPlaybackPosition(positionMs: Long) {
        playbackPosition = positionMs
    }

    fun updateFrom(exoPlayer: ExoPlayer) {
        playbackPosition = exoPlayer.currentPosition
        playWhenReady = exoPlayer.playWhenReady
        currentMediaItemIndex = exoPlayer.currentMediaItemIndex
    }
}
