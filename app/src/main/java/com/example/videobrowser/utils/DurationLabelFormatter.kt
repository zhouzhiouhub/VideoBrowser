package com.example.videobrowser.utils

import java.util.Locale
import java.util.concurrent.TimeUnit

object DurationLabelFormatter {
    enum class MinuteStyle {
        MINIMAL,
        TWO_DIGIT
    }

    fun formatMillis(
        durationMs: Long,
        minuteStyle: MinuteStyle = MinuteStyle.MINIMAL
    ): String {
        return formatSeconds(TimeUnit.MILLISECONDS.toSeconds(durationMs.coerceAtLeast(0L)), minuteStyle)
    }

    fun formatSeconds(
        totalSeconds: Long,
        minuteStyle: MinuteStyle = MinuteStyle.MINIMAL
    ): String {
        val safeSeconds = totalSeconds.coerceAtLeast(0L)
        val hours = safeSeconds / SECONDS_PER_HOUR
        val minutes = (safeSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
        val seconds = safeSeconds % SECONDS_PER_MINUTE

        return if (hours > 0L) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            when (minuteStyle) {
                MinuteStyle.MINIMAL -> String.format(Locale.US, "%d:%02d", minutes, seconds)
                MinuteStyle.TWO_DIGIT -> String.format(Locale.US, "%02d:%02d", minutes, seconds)
            }
        }
    }

    private const val SECONDS_PER_MINUTE = 60L
    private const val SECONDS_PER_HOUR = 60L * SECONDS_PER_MINUTE
}
