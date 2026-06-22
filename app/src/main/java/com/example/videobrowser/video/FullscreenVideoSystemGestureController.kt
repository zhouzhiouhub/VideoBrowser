package com.example.videobrowser.video

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.provider.Settings

internal class FullscreenVideoSystemGestureController(
    private val activity: Activity,
    private val showFeedback: (String) -> Unit
) {
    private val audioManager =
        activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var savedWindowBrightness: Float? = null

    fun captureWindowBrightness() {
        savedWindowBrightness = activity.window.attributes.screenBrightness
    }

    fun restoreWindowBrightness() {
        val saved = savedWindowBrightness ?: return
        val attributes = activity.window.attributes
        attributes.screenBrightness = saved
        activity.window.attributes = attributes
        savedWindowBrightness = null
    }

    fun currentWindowBrightness(): Float {
        val current = activity.window.attributes.screenBrightness
        if (current >= 0f) {
            return FullscreenVideoGestureMath.clampBrightness(current)
        }
        return runCatching {
            Settings.System.getInt(
                activity.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            ) / SCREEN_BRIGHTNESS_SCALE
        }.getOrDefault(FullscreenVideoGestureMath.DEFAULT_BRIGHTNESS)
            .let(FullscreenVideoGestureMath::clampBrightness)
    }

    fun currentStreamVolume(): Int {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    fun updateBrightness(deltaY: Float, viewHeight: Int, initialBrightness: Float) {
        val brightness = FullscreenVideoGestureMath.brightnessForDrag(
            initialBrightness = initialBrightness,
            deltaY = deltaY,
            viewHeight = viewHeight
        )
        val attributes = activity.window.attributes
        attributes.screenBrightness = brightness
        activity.window.attributes = attributes
        showFeedback(VideoGestureFeedbackFormatter.formatBrightness(brightness))
    }

    fun updateVolume(deltaY: Float, viewHeight: Int, initialVolume: Int) {
        val minVolume = streamMinVolume()
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val nextVolume = FullscreenVideoGestureMath.volumeForDrag(
            initialVolume = initialVolume,
            deltaY = deltaY,
            viewHeight = viewHeight,
            minVolume = minVolume,
            maxVolume = maxVolume
        ) ?: return
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, nextVolume, 0)
        showFeedback(VideoGestureFeedbackFormatter.formatVolume(nextVolume, minVolume, maxVolume))
    }

    private fun streamMinVolume(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
        } else {
            0
        }
    }

    private companion object {
        private const val SCREEN_BRIGHTNESS_SCALE = 255f
    }
}
