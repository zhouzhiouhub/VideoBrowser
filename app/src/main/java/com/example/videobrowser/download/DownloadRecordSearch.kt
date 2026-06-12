package com.example.videobrowser.download

import java.util.Locale

object DownloadRecordSearch {
    fun filter(records: List<DownloadRecord>, query: String?): List<DownloadRecord> {
        val terms = query
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?.split(Regex("\\s+"))
            ?.filter { term -> term.isNotBlank() }
            ?: emptyList()
        if (terms.isEmpty()) {
            return records
        }

        return records.filter { record ->
            val haystack = listOf(
                record.title,
                record.fileName,
                record.sourceUrl,
                record.mimeType.orEmpty()
            ).joinToString(separator = "\n").lowercase(Locale.ROOT)
            terms.all { term -> haystack.contains(term) }
        }
    }
}
