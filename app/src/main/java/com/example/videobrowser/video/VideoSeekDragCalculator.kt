package com.example.videobrowser.video

import kotlin.math.roundToLong

object VideoSeekDragCalculator {
    private const val UNKNOWN_DURATION_SEEK_SPAN_MS = 60_000L
    private const val MAX_KNOWN_DURATION_SEEK_SPAN_MS = 10L * 60L * 1000L

    fun seekSpanForDuration(durationMs: Long?): Long {
        return durationMs
            ?.takeIf { it > 0L }
            ?.coerceAtMost(MAX_KNOWN_DURATION_SEEK_SPAN_MS)
            ?: UNKNOWN_DURATION_SEEK_SPAN_MS
    }

    fun offsetForDrag(deltaX: Float, viewWidth: Int, durationMs: Long?): Long {
        if (viewWidth <= 0) return 0L

        val ratio = (deltaX / viewWidth.toFloat()).coerceIn(-1f, 1f)
        return (ratio * seekSpanForDuration(durationMs)).roundToLong()
    }

    fun targetForDrag(
        startPositionMs: Long,
        durationMs: Long?,
        deltaX: Float,
        viewWidth: Int
    ): Long {
        val target = startPositionMs + offsetForDrag(deltaX, viewWidth, durationMs)
        return durationMs
            ?.takeIf { it > 0L }
            ?.let { target.coerceIn(0L, it) }
            ?: target.coerceAtLeast(0L)
    }
}
