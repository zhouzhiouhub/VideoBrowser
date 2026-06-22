package com.example.videobrowser.video

import com.example.videobrowser.utils.PlaybackSpeedNormalizer
import kotlin.math.roundToInt

internal object FullscreenVideoGestureMath {
    fun clampBrightness(brightness: Float): Float {
        return brightness.coerceIn(MIN_BRIGHTNESS, MAX_BRIGHTNESS)
    }

    fun brightnessForDrag(
        initialBrightness: Float,
        deltaY: Float,
        viewHeight: Int
    ): Float {
        if (viewHeight <= 0) {
            return clampBrightness(initialBrightness)
        }

        val deltaRatio = -deltaY / viewHeight.toFloat()
        return clampBrightness(initialBrightness + deltaRatio)
    }

    fun volumeForDrag(
        initialVolume: Int,
        deltaY: Float,
        viewHeight: Int,
        minVolume: Int,
        maxVolume: Int
    ): Int? {
        if (maxVolume <= minVolume || viewHeight <= 0) {
            return null
        }

        val deltaRatio = -deltaY / viewHeight.toFloat()
        return (initialVolume + deltaRatio * (maxVolume - minVolume))
            .roundToInt()
            .coerceIn(minVolume, maxVolume)
    }

    fun volumePercent(volume: Int, minVolume: Int, maxVolume: Int): Int {
        if (maxVolume <= minVolume) return 0

        return (((volume - minVolume).toFloat() / (maxVolume - minVolume)) * 100)
            .roundToInt()
            .coerceIn(0, 100)
    }

    fun normalizeSpeed(speed: Float, defaultSpeed: Float = DEFAULT_PLAYBACK_SPEED): Float {
        return PlaybackSpeedNormalizer.normalize(speed, defaultSpeed)
    }

    fun screenZoneFor(
        x: Float,
        width: Int,
        leftZoneRatio: Float = LEFT_ZONE_RATIO,
        rightZoneRatio: Float = RIGHT_ZONE_RATIO
    ): VideoGestureScreenZone {
        return when {
            x < 0f || width <= 0 -> VideoGestureScreenZone.NONE
            x < width * leftZoneRatio -> VideoGestureScreenZone.LEFT
            x >= width * (1f - rightZoneRatio) -> VideoGestureScreenZone.RIGHT
            else -> VideoGestureScreenZone.CENTER
        }
    }

    private const val DEFAULT_PLAYBACK_SPEED = 1f
    private const val MIN_BRIGHTNESS = 0.02f
    private const val MAX_BRIGHTNESS = 1f
    private const val LEFT_ZONE_RATIO = 0.3f
    private const val RIGHT_ZONE_RATIO = 0.3f
}

internal enum class VideoGestureScreenZone {
    LEFT,
    CENTER,
    RIGHT,
    NONE
}

internal fun VideoGestureScreenZone.isSide(): Boolean {
    return this == VideoGestureScreenZone.LEFT || this == VideoGestureScreenZone.RIGHT
}
