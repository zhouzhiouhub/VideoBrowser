package com.example.videobrowser.utils

import java.util.Locale

object PlaybackSpeedDisplayFormatter {
    fun format(speed: Float): String {
        val normalized = PlaybackSpeedNormalizer.normalize(speed)
        val numeric = if (normalized % 1f == 0f) {
            normalized.toInt().toString()
        } else {
            String.format(Locale.US, "%.2f", normalized).trimEnd('0').trimEnd('.')
        }
        return "${numeric}x"
    }
}
