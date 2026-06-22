package com.example.videobrowser.video

import androidx.media3.exoplayer.ExoPlayer

internal class NativePlaybackHistorySessionController(
    private val historyController: NativePlaybackHistoryController,
    private val playbackQueue: () -> PlaybackQueue,
    private val currentMediaItemIndex: () -> Int,
    private val fallbackMediaUri: () -> String,
    private val fallbackMediaTitle: () -> String?,
    private val currentPlaybackSpeed: () -> Float
) {
    fun restore(): NativePlaybackHistoryRestore {
        return historyController.restore(playbackHistoryIdentity())
    }

    fun restorePositionForCurrentMedia(): Long {
        return restore().positionMs ?: 0L
    }

    fun save(exoPlayer: ExoPlayer) {
        historyController.save(
            NativePlaybackHistorySnapshot(
                mediaIdentity = playbackHistoryIdentity(),
                positionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
                durationMs = Media3Duration.durationOrZero(exoPlayer.duration),
                speed = currentPlaybackSpeed(),
                title = currentMediaTitle()
            )
        )
    }

    private fun playbackHistoryIdentity(): String {
        return currentMediaItem()?.uri?.trim()
            ?: fallbackMediaUri().trim()
    }

    private fun currentMediaTitle(): String? {
        return currentMediaItem()?.title ?: fallbackMediaTitle()
    }

    private fun currentMediaItem(): PlayableMediaItem? {
        return playbackQueue().items.getOrNull(currentMediaItemIndex())
    }
}
