package com.example.videobrowser.download

object DownloadRecordFilter {
    fun filter(
        records: List<DownloadRecord>,
        status: DownloadStatus? = null,
        category: DownloadCategory? = null
    ): List<DownloadRecord> {
        return records.filter { record ->
            val statusMatches = status == null || record.status == status
            val categoryMatches = category == null ||
                DownloadCategory.from(record.mimeType, record.fileName) == category
            statusMatches && categoryMatches
        }
    }
}
