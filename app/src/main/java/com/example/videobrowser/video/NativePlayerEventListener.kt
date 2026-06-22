package com.example.videobrowser.video

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player

/**
 * 初学者阅读提示：
 * 这个文件属于“原生播放器事件模块”。
 * 文件名 NativePlayerEventListener 可以拆开理解为“Native Player Event Listener”，表示它只处理 ExoPlayer 回调事件的分发。
 * 主要职责：记录播放事件日志，并把错误重试、播放历史保存、控制条唤醒和媒体切换交给 PlayerActivity 提供的回调。
 * 阅读顺序：先看构造参数知道每个事件会调用哪个回调，再看各个 Player.Listener override。
 */
internal class NativePlayerEventListener(
    private val logTag: String,
    private val isVideoEffectsEnabled: () -> Boolean,
    private val hasRetriedPlaybackWithoutVideoEffects: () -> Boolean,
    private val retryPlaybackWithoutVideoEffects: () -> Unit,
    private val showPlaybackFailed: () -> Unit,
    private val savePlaybackHistory: () -> Unit,
    private val wakePlayerControls: () -> Unit,
    private val mediaItemTransitioned: () -> Unit
) : Player.Listener {
    override fun onPlayerError(error: PlaybackException) {
        Log.d(
            logTag,
            "event=native-player-error code=${error.errorCode} message=${error.message}"
        )
        if (isVideoEffectsEnabled() && !hasRetriedPlaybackWithoutVideoEffects()) {
            retryPlaybackWithoutVideoEffects()
            return
        }
        showPlaybackFailed()
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        Log.d(
            logTag,
            "event=native-play-when-ready playWhenReady=$playWhenReady reason=$reason"
        )
        savePlaybackHistory()
        wakePlayerControls()
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        Log.d(logTag, "event=native-playback-state state=$playbackState")
        if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED) {
            savePlaybackHistory()
        }
        wakePlayerControls()
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        mediaItemTransitioned()
        wakePlayerControls()
    }
}
