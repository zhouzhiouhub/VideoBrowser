package com.example.videobrowser.functioncenter

import java.util.Locale

object BrowserDataDisplayFormatter {
    fun siteDataUsageSummary(usageBytes: Long): String {
        return formatBytes(usageBytes)
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) {
            return "0 B"
        }
        val units = arrayOf("B", "KB", "MB", "GB")
        var value = bytes.toDouble()
        var unitIndex = 0
        while (value >= 1024.0 && unitIndex < units.lastIndex) {
            value /= 1024.0
            unitIndex += 1
        }
        return String.format(Locale.US, "%.1f %s", value, units[unitIndex])
    }
}
