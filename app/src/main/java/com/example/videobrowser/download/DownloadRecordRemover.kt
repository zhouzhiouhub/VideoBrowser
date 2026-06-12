package com.example.videobrowser.download

data class DownloadRecordRemoveResult(
    val requestedSystemDownloadCount: Int,
    val removedSystemDownloadCount: Int,
    val recordRemoved: Boolean
)

class DownloadRecordRemover(
    private val downloadRecordRepository: DownloadRecordRepository,
    private val systemDownloadRemover: SystemDownloadRemover
) {
    fun remove(record: DownloadRecord): DownloadRecordRemoveResult {
        val removedCount = runCatching {
            systemDownloadRemover.remove(longArrayOf(record.downloadId))
        }.getOrDefault(0).coerceAtLeast(0)
        return DownloadRecordRemoveResult(
            requestedSystemDownloadCount = 1,
            removedSystemDownloadCount = removedCount,
            recordRemoved = downloadRecordRepository.remove(record.downloadId)
        )
    }
}
