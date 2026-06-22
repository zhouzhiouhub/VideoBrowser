package com.example.videobrowser.video

import android.util.Log
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

/**
 * 初学者阅读提示：
 * 这个文件属于“原生播放器基础操作模块”。
 * 文件名 NativePlayerTransportController 可以拆开理解为“Native Player Transport Controller”，表示它只负责 ExoPlayer 的播放、暂停和进度跳转。
 * 主要职责：把 seek/play/pause/toggle/current seek position 这些直接操作 ExoPlayer 的代码从 Activity 中集中出来。
 * 阅读顺序：先看 seekBy/seekTo，再看 play/pause/togglePlayPause，最后看 currentSeekPosition。
 */
internal class NativePlayerTransportController(
    private val player: () -> ExoPlayer?,
    private val logTag: String,
    private val wakePlayerControls: () -> Unit
) {
    fun seekBy(offsetMs: Long) {
        val exoPlayer = player() ?: return
        seekTo(exoPlayer.currentPosition + offsetMs)
    }

    fun seekTo(positionMs: Long) {
        val exoPlayer = player() ?: return
        val boundedTarget = Media3Duration.boundedSeekPositionMs(positionMs, exoPlayer.duration)
        exoPlayer.seekTo(boundedTarget)
    }

    fun currentSeekPosition(): FullscreenVideoGestureOverlay.SeekPosition? {
        val exoPlayer = player() ?: return null
        val duration = Media3Duration.knownDurationMs(exoPlayer.duration)
        return FullscreenVideoGestureOverlay.SeekPosition(
            positionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
            durationMs = duration
        )
    }

    fun togglePlayPause(): Boolean? {
        val exoPlayer = player() ?: return null
        Log.d(
            logTag,
            "event=native-toggle-playback isPlaying=${exoPlayer.isPlaying} " +
                "playWhenReady=${exoPlayer.playWhenReady} state=${exoPlayer.playbackState}"
        )
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (exoPlayer.playbackState == Player.STATE_ENDED) {
                exoPlayer.seekTo(0)
            }
            exoPlayer.play()
        }
        wakePlayerControls()
        return exoPlayer.playWhenReady
    }

    fun play(): Boolean? {
        val exoPlayer = player() ?: return null
        if (exoPlayer.playbackState == Player.STATE_ENDED) {
            exoPlayer.seekTo(0)
        }
        exoPlayer.play()
        wakePlayerControls()
        return exoPlayer.playWhenReady
    }

    fun pause(): Boolean? {
        val exoPlayer = player() ?: return null
        exoPlayer.pause()
        wakePlayerControls()
        return exoPlayer.playWhenReady
    }
}
