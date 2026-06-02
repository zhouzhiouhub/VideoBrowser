package com.example.videobrowser.localfiles

import android.content.Context
import com.example.videobrowser.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalDocumentFormatter(
    private val context: Context
) {
    fun summary(document: LocalDocument): String {
        if (document.isDirectory) {
            return context.getString(R.string.local_file_type_folder)
        }

        val type = document.mimeType
            ?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.local_file_type_unknown)
        return listOf(
            type,
            formatFileSize(document.size),
            formatModifiedTime(document.modifiedAt)
        ).joinToString(separator = " · ")
    }

    private fun formatFileSize(size: Long?): String {
        if (size == null || size < 0) {
            return context.getString(R.string.local_file_size_unknown)
        }

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var value = size.toDouble()
        var unitIndex = 0
        while (value >= 1024 && unitIndex < units.lastIndex) {
            value /= 1024
            unitIndex++
        }
        return if (unitIndex == 0) {
            "$size ${units[unitIndex]}"
        } else {
            String.format(Locale.getDefault(), "%.1f %s", value, units[unitIndex])
        }
    }

    private fun formatModifiedTime(modifiedAt: Long?): String {
        if (modifiedAt == null || modifiedAt <= 0L) {
            return context.getString(R.string.local_file_time_unknown)
        }
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .format(Date(modifiedAt))
    }
}
