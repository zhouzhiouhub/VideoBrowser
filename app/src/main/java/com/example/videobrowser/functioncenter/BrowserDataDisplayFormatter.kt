package com.example.videobrowser.functioncenter

import com.example.videobrowser.utils.ByteSizeFormatter
import java.util.Locale

object BrowserDataDisplayFormatter {
    fun siteDataUsageSummary(usageBytes: Long): String {
        return formatBytes(usageBytes)
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) {
            return "0 B"
        }
        return ByteSizeFormatter.format(
            bytes,
            maxUnit = ByteSizeFormatter.MaxUnit.GB,
            locale = Locale.US,
            decimalBytes = true
        )
    }
}
