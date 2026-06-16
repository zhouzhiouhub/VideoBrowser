package com.example.videobrowser.video

/**
 * 初学者阅读提示：
 * 这个文件属于“视频播放模块”。
 * 文件名 PlaybackSessionState 可以拆开理解为“Playback Session State”，表示它只负责视频链路中的一个小职责。
 * 主要职责：连接网页视频手势、原生 ExoPlayer 播放、播放队列、字幕、播放历史或媒体路由。
 * 阅读顺序：先看数据模型表达什么播放状态，再看控制器如何响应用户手势和播放器回调。
 */
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
