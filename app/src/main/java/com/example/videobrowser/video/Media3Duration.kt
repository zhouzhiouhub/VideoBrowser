package com.example.videobrowser.video

import androidx.media3.common.C

internal object Media3Duration {
    fun knownDurationMs(durationMs: Long): Long? {
        return durationMs.takeIf { value -> value != C.TIME_UNSET && value > 0L }
    }

    fun durationOrZero(durationMs: Long): Long {
        return knownDurationMs(durationMs) ?: 0L
    }

    fun boundedSeekPositionMs(positionMs: Long, durationMs: Long): Long {
        return PlaybackSeekBounds.clampPosition(positionMs, knownDurationMs(durationMs))
    }
}
