package com.example.videobrowser.video

data class PlaybackSessionState(
    val positionMs: Long = 0L,
    val durationMs: Long? = null,
    val speed: Float = 1f,
    val repeatMode: PlaybackRepeatMode = PlaybackRepeatMode.NONE,
    val currentIndex: Int = 0,
    val playWhenReady: Boolean = true,
    val zoomMode: VideoZoomMode = VideoZoomMode.FIT
) {
    fun normalized(itemCount: Int): PlaybackSessionState {
        return copy(
            positionMs = positionMs.coerceAtLeast(0L),
            durationMs = durationMs?.takeIf { it > 0L },
            speed = normalizeSpeed(speed),
            currentIndex = if (itemCount > 0) {
                currentIndex.coerceIn(0, itemCount - 1)
            } else {
                0
            }
        )
    }

    companion object {
        fun fromQueue(
            queue: PlaybackQueue,
            positionMs: Long,
            durationMs: Long?,
            speed: Float,
            playWhenReady: Boolean,
            zoomMode: VideoZoomMode
        ): PlaybackSessionState {
            return PlaybackSessionState(
                positionMs = positionMs,
                durationMs = durationMs,
                speed = speed,
                repeatMode = queue.repeatMode,
                currentIndex = queue.currentIndex,
                playWhenReady = playWhenReady,
                zoomMode = zoomMode
            ).normalized(queue.items.size)
        }

        private fun normalizeSpeed(speed: Float): Float {
            return if (!speed.isNaN() && !speed.isInfinite() && speed > 0f) {
                speed
            } else {
                1f
            }
        }
    }
}

sealed class PlaybackCommand {
    object Play : PlaybackCommand()
    object Pause : PlaybackCommand()
    object TogglePlayPause : PlaybackCommand()
    data class SeekBy(val offsetMs: Long) : PlaybackCommand()
    data class SeekTo(val positionMs: Long) : PlaybackCommand()
    data class SetSpeed(val speed: Float) : PlaybackCommand()
    object Previous : PlaybackCommand()
    object Next : PlaybackCommand()
    object ToggleRepeat : PlaybackCommand()
    data class SelectQueueItem(val index: Int) : PlaybackCommand()
    object ShowQueue : PlaybackCommand()
    object ToggleShuffle : PlaybackCommand()
    object CycleZoom : PlaybackCommand()
    object ShowTrackSelection : PlaybackCommand()
    data class SelectTrack(val trackType: PlaybackTrackType) : PlaybackCommand()
}

enum class PlaybackTrackType {
    AUDIO,
    SUBTITLE
}
