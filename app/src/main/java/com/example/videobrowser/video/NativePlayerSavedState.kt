package com.example.videobrowser.video

import android.os.Bundle
import com.example.videobrowser.utils.PlaybackSpeedNormalizer

internal data class NativePlayerSavedState(
    val playbackQueue: PlaybackQueue,
    val playbackPosition: Long,
    val playWhenReady: Boolean,
    val currentMediaItemIndex: Int,
    val isLandscape: Boolean,
    val selectedPlaybackSpeed: Float,
    val repeatMode: PlaybackRepeatMode,
    val videoZoomMode: VideoZoomMode,
    val videoEffectsEnabled: Boolean,
    val retriedPlaybackWithoutVideoEffects: Boolean
) {
    companion object {
        fun restore(
            savedInstanceState: Bundle?,
            fallbackPlaybackQueue: PlaybackQueue,
            fallbackPlaybackSpeed: Float,
            fallbackVideoZoomMode: VideoZoomMode = VideoZoomMode.FIT
        ): NativePlayerSavedState? {
            if (savedInstanceState == null) {
                return null
            }

            val restoredQueue = savedInstanceState.getString(STATE_PLAYBACK_QUEUE)
                ?.let(PlaybackQueueJsonCodec::decode)
                ?: fallbackPlaybackQueue
            val restoredIndex = savedInstanceState.getInt(STATE_MEDIA_ITEM_INDEX)
                .coerceToQueueIndex(restoredQueue)
            val restoredRepeatMode = savedInstanceState.getString(STATE_REPEAT_MODE)
                .enumValueOrNull<PlaybackRepeatMode>()
                ?: restoredQueue.repeatMode
            val restoredPlaybackQueue = restoredQueue
                .select(restoredIndex)
                .copy(repeatMode = restoredRepeatMode)

            return NativePlayerSavedState(
                playbackQueue = restoredPlaybackQueue,
                playbackPosition = savedInstanceState.getLong(STATE_PLAYBACK_POSITION),
                playWhenReady = savedInstanceState.getBoolean(STATE_PLAY_WHEN_READY, true),
                currentMediaItemIndex = restoredIndex,
                isLandscape = savedInstanceState.getBoolean(STATE_LANDSCAPE, true),
                selectedPlaybackSpeed = PlaybackSpeedNormalizer.normalize(
                    savedInstanceState.getFloat(STATE_PLAYBACK_SPEED, fallbackPlaybackSpeed)
                ),
                repeatMode = restoredRepeatMode,
                videoZoomMode = savedInstanceState.getString(STATE_VIDEO_ZOOM_MODE)
                    .enumValueOrNull<VideoZoomMode>()
                    ?: fallbackVideoZoomMode,
                videoEffectsEnabled = savedInstanceState.getBoolean(
                    STATE_VIDEO_EFFECTS_ENABLED,
                    true
                ),
                retriedPlaybackWithoutVideoEffects = savedInstanceState.getBoolean(
                    STATE_RETRIED_WITHOUT_VIDEO_EFFECTS,
                    false
                )
            )
        }

        fun save(
            outState: Bundle,
            sessionState: PlaybackSessionState,
            playbackQueue: PlaybackQueue,
            isLandscape: Boolean,
            videoEffectsEnabled: Boolean,
            retriedPlaybackWithoutVideoEffects: Boolean
        ) {
            outState.putLong(STATE_PLAYBACK_POSITION, sessionState.positionMs)
            outState.putBoolean(STATE_PLAY_WHEN_READY, sessionState.playWhenReady)
            outState.putInt(STATE_MEDIA_ITEM_INDEX, sessionState.currentIndex)
            outState.putBoolean(STATE_LANDSCAPE, isLandscape)
            outState.putFloat(STATE_PLAYBACK_SPEED, sessionState.speed)
            outState.putString(STATE_REPEAT_MODE, sessionState.repeatMode.name)
            outState.putString(STATE_PLAYBACK_QUEUE, PlaybackQueueJsonCodec.encode(playbackQueue))
            outState.putString(STATE_VIDEO_ZOOM_MODE, sessionState.zoomMode.name)
            outState.putBoolean(STATE_VIDEO_EFFECTS_ENABLED, videoEffectsEnabled)
            outState.putBoolean(
                STATE_RETRIED_WITHOUT_VIDEO_EFFECTS,
                retriedPlaybackWithoutVideoEffects
            )
        }

        private fun Int.coerceToQueueIndex(queue: PlaybackQueue): Int {
            return if (queue.items.isEmpty()) {
                0
            } else {
                coerceIn(0, queue.items.lastIndex)
            }
        }

        private inline fun <reified T : Enum<T>> String?.enumValueOrNull(): T? {
            return this?.let { value ->
                runCatching { enumValueOf<T>(value) }.getOrNull()
            }
        }

        private const val STATE_PLAYBACK_POSITION = "playback_position"
        private const val STATE_PLAY_WHEN_READY = "play_when_ready"
        private const val STATE_MEDIA_ITEM_INDEX = "media_item_index"
        private const val STATE_LANDSCAPE = "landscape"
        private const val STATE_PLAYBACK_SPEED = "playback_speed"
        private const val STATE_REPEAT_MODE = "repeat_mode"
        private const val STATE_PLAYBACK_QUEUE = "playback_queue"
        private const val STATE_VIDEO_ZOOM_MODE = "video_zoom_mode"
        private const val STATE_VIDEO_EFFECTS_ENABLED = "video_effects_enabled"
        private const val STATE_RETRIED_WITHOUT_VIDEO_EFFECTS = "retried_without_video_effects"
    }
}
