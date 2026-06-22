package com.example.videobrowser.video

import androidx.media3.exoplayer.ExoPlayer

internal class NativePlaybackSessionStateProvider(
    private val playerProvider: () -> ExoPlayer?,
    private val playbackQueue: () -> PlaybackQueue,
    private val fallbackPlaybackPosition: () -> Long,
    private val fallbackPlayWhenReady: () -> Boolean,
    private val currentPlaybackSpeed: () -> Float,
    private val currentVideoZoomMode: () -> VideoZoomMode
) {
    fun currentState(): PlaybackSessionState {
        val exoPlayer = playerProvider()
        val durationMs = exoPlayer
            ?.duration
            ?.let(Media3Duration::knownDurationMs)
        return PlaybackSessionState.fromQueue(
            queue = playbackQueue(),
            positionMs = exoPlayer?.currentPosition ?: fallbackPlaybackPosition(),
            durationMs = durationMs,
            speed = currentPlaybackSpeed(),
            playWhenReady = exoPlayer?.playWhenReady ?: fallbackPlayWhenReady(),
            zoomMode = currentVideoZoomMode()
        )
    }
}
