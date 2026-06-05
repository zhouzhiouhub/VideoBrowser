package com.example.videobrowser.download

fun interface SystemDownloadRemover {
    fun remove(downloadIds: LongArray): Int
}

data class DownloadRecordClearResult(
    val requestedSystemDownloadCount: Int,
    val removedSystemDownloadCount: Int
)

class DownloadRecordCleaner(
    private val downloadRecordRepository: DownloadRecordRepository,
    private val systemDownloadRemover: SystemDownloadRemover
) {
    fun clearRecordsAndFiles(): DownloadRecordClearResult {
        val downloadIds = downloadRecordRepository.records()
            .map { record -> record.downloadId }
            .distinct()
            .toLongArray()
        val removedCount = if (downloadIds.isEmpty()) {
            0
        } else {
            runCatching { systemDownloadRemover.remove(downloadIds) }.getOrDefault(0)
        }

        downloadRecordRepository.clear()
        return DownloadRecordClearResult(
            requestedSystemDownloadCount = downloadIds.size,
            removedSystemDownloadCount = removedCount
        )
    }
}
