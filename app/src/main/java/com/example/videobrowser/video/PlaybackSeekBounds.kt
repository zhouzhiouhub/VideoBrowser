package com.example.videobrowser.video

internal object PlaybackSeekBounds {
    fun clampPosition(positionMs: Long, durationMs: Long?): Long {
        return durationMs
            ?.takeIf { duration -> duration > 0L }
            ?.let { duration -> positionMs.coerceIn(0L, duration) }
            ?: positionMs.coerceAtLeast(0L)
    }

    fun offsetPosition(
        currentPositionMs: Long?,
        offsetMs: Long,
        durationMs: Long?
    ): Long? {
        val currentPosition = currentPositionMs ?: return null
        return clampPosition(currentPosition + offsetMs, durationMs)
    }
}
