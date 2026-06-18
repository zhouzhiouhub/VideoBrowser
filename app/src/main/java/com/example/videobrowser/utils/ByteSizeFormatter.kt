package com.example.videobrowser.utils

import java.util.Locale

object ByteSizeFormatter {
    enum class MaxUnit {
        GB,
        TB
    }

    fun format(
        bytes: Long,
        maxUnit: MaxUnit = MaxUnit.TB,
        locale: Locale = Locale.US,
        decimalBytes: Boolean = false
    ): String {
        val units = unitsFor(maxUnit)
        val safeBytes = bytes.coerceAtLeast(0L)
        var value = safeBytes.toDouble()
        var unitIndex = 0
        while (value >= BYTES_PER_UNIT && unitIndex < units.lastIndex) {
            value /= BYTES_PER_UNIT
            unitIndex += 1
        }
        return if (unitIndex == 0 && !decimalBytes) {
            "$safeBytes ${units[unitIndex]}"
        } else {
            String.format(locale, "%.1f %s", value, units[unitIndex])
        }
    }

    private fun unitsFor(maxUnit: MaxUnit): Array<String> {
        return when (maxUnit) {
            MaxUnit.GB -> arrayOf("B", "KB", "MB", "GB")
            MaxUnit.TB -> arrayOf("B", "KB", "MB", "GB", "TB")
        }
    }

    private const val BYTES_PER_UNIT = 1024.0
}
