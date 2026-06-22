package com.example.videobrowser.video

import androidx.media3.exoplayer.ExoPlayer

/**
 * Coordinates native player queue mutations with the current ExoPlayer instance.
 */
internal class NativePlayerQueueController(
    private val playerProvider: () -> ExoPlayer?,
    private val queueProvider: () -> PlaybackQueue,
    private val setQueue: (PlaybackQueue) -> Unit,
    private val currentMediaItemIndex: () -> Int,
    private val setCurrentMediaItemIndex: (Int) -> Unit,
    private val playbackPosition: () -> Long,
    private val setPlaybackPosition: (Long) -> Unit,
    private val playWhenReady: () -> Boolean,
    private val savePlayerState: () -> Unit,
    private val restorePlaybackPositionForCurrentMedia: () -> Long,
    private val applyRepeatModeToQueue: (PlaybackQueue) -> PlaybackQueue,
    private val applyRepeatModeToPlayer: (ExoPlayer) -> Unit,
    private val applyPlaybackSpeedToPlayer: (ExoPlayer) -> Unit,
    private val updateTitle: (String) -> Unit,
    private val updateQueueControls: () -> Unit,
    private val wakePlayerControls: () -> Unit
) {
    fun handleMediaItemTransition(index: Int) {
        setCurrentMediaItemIndex(index)
        val selectedQueue = applyRepeatModeToQueue(queueProvider().select(index))
        setQueue(selectedQueue)
        setPlaybackPosition(0L)
        updateTitle(selectedQueue.items.getOrNull(index)?.title.orEmpty())
        updateQueueControls()
    }

    fun playPreviousMedia() {
        val playbackQueue = queueProvider()
        playMediaAt(playbackQueue.previous().currentIndex)
    }

    fun playNextMedia() {
        val playbackQueue = queueProvider()
        playMediaAt(playbackQueue.next().currentIndex)
    }

    fun playMediaAt(index: Int) {
        val exoPlayer = playerProvider() ?: return
        val playbackQueue = queueProvider()
        if (index !in playbackQueue.items.indices) {
            wakePlayerControls()
            return
        }
        if (index == currentMediaItemIndex()) {
            setQueue(playbackQueue.select(index))
            wakePlayerControls()
            return
        }

        savePlayerState()
        val selectedQueue = playbackQueue.select(index)
        setQueue(selectedQueue)
        setCurrentMediaItemIndex(selectedQueue.currentIndex)
        val restoredPosition = restorePlaybackPositionForCurrentMedia()
        setPlaybackPosition(restoredPosition)
        exoPlayer.seekTo(index, restoredPosition)
        exoPlayer.play()
        updateTitle(selectedQueue.items[index].title.orEmpty())
        updateQueueControls()
        wakePlayerControls()
    }

    fun toggleShuffleMode(): Boolean {
        val playbackQueue = queueProvider()
        if (!playbackQueue.hasMultipleItems) {
            wakePlayerControls()
            return playbackQueue.isShuffled
        }
        savePlayerState()
        val shuffledQueue = playbackQueue.toggleShuffle()
        setQueue(shuffledQueue)
        setCurrentMediaItemIndex(shuffledQueue.currentIndex)
        syncPlayerQueueToPlaybackQueue()
        updateQueueControls()
        wakePlayerControls()
        return shuffledQueue.isShuffled
    }

    fun syncPlayerQueueToPlaybackQueue() {
        val exoPlayer = playerProvider() ?: return
        val playbackQueue = queueProvider()
        exoPlayer.setMediaItems(
            playbackQueue.items.map(PlayableMediaItemMedia3Converter::toMediaItem),
            currentMediaItemIndex(),
            playbackPosition()
        )
        applyRepeatModeToPlayer(exoPlayer)
        applyPlaybackSpeedToPlayer(exoPlayer)
        exoPlayer.playWhenReady = playWhenReady()
        exoPlayer.prepare()
    }

    fun removeMediaFromQueue(index: Int) {
        val playbackQueue = queueProvider()
        if (!playbackQueue.canRemoveAt(index)) {
            wakePlayerControls()
            return
        }
        savePlayerState()
        val removedCurrentItem = index == currentMediaItemIndex()
        val updatedQueue = playbackQueue.removeAt(index)
        setQueue(updatedQueue)
        setCurrentMediaItemIndex(updatedQueue.currentIndex)
        val exoPlayer = playerProvider()
        if (exoPlayer != null && index < exoPlayer.mediaItemCount) {
            exoPlayer.removeMediaItem(index)
            if (removedCurrentItem) {
                val restoredPosition = restorePlaybackPositionForCurrentMedia()
                setPlaybackPosition(restoredPosition)
                exoPlayer.seekTo(updatedQueue.currentIndex, restoredPosition)
                exoPlayer.play()
            }
        }
        updateTitle(updatedQueue.currentItem()?.title.orEmpty())
        updateQueueControls()
        wakePlayerControls()
    }
}
