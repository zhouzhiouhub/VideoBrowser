package com.example.videobrowser.utils

internal object PlaybackSpeedNormalizer {
    fun normalize(speed: Float, defaultSpeed: Float = DEFAULT_SPEED): Float {
        return if (!speed.isNaN() && !speed.isInfinite() && speed > 0f) {
            speed
        } else {
            defaultSpeed
        }
    }

    private const val DEFAULT_SPEED = 1f
}
