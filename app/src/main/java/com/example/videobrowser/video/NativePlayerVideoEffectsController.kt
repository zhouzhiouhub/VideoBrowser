package com.example.videobrowser.video

import android.util.Log
import androidx.media3.common.Effect
import androidx.media3.exoplayer.ExoPlayer

internal class NativePlayerVideoEffectsController(
    private val logTag: String,
    private val defaultEffects: () -> List<Effect> = NativeVideoEnhancement::defaultEffects
) {
    private var videoEffectsEnabled = true
    private var retriedPlaybackWithoutVideoEffects = false

    fun isEnabled(): Boolean {
        return videoEffectsEnabled
    }

    fun hasRetriedWithoutEffects(): Boolean {
        return retriedPlaybackWithoutVideoEffects
    }

    fun restoreState(
        enabled: Boolean,
        retriedWithoutEffects: Boolean
    ) {
        videoEffectsEnabled = enabled
        retriedPlaybackWithoutVideoEffects = retriedWithoutEffects
    }

    fun applyToPlayer(exoPlayer: ExoPlayer) {
        val videoEffects = defaultEffects()
        if (!videoEffectsEnabled || videoEffects.isEmpty()) {
            return
        }

        runCatching {
            exoPlayer.setVideoEffects(videoEffects)
        }.onSuccess {
            Log.d(
                logTag,
                "event=native-video-effects applied=true count=${videoEffects.size}"
            )
        }.onFailure { error ->
            videoEffectsEnabled = false
            Log.d(
                logTag,
                "event=native-video-effects applied=false error=${error.message}"
            )
        }
    }

    fun markRetryWithoutEffects(): Boolean {
        if (retriedPlaybackWithoutVideoEffects) {
            return false
        }
        Log.d(logTag, "event=native-video-effects retryWithoutEffects=true")
        retriedPlaybackWithoutVideoEffects = true
        videoEffectsEnabled = false
        return true
    }
}
