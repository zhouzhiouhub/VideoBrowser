package com.example.videobrowser.download

data class DownloadCancellationResult(
    val requestedSystemDownloadCount: Int,
    val removedSystemDownloadCount: Int,
    val statusUpdated: Boolean
) {
    val canceled: Boolean
        get() = removedSystemDownloadCount > 0 && statusUpdated
}

class DownloadCanceller(
    private val downloadRecordRepository: DownloadRecordRepository,
    private val systemDownloadRemover: SystemDownloadRemover
) {
    fun cancel(record: DownloadRecord): DownloadCancellationResult {
        if (!DownloadCancellationPolicy.canCancel(record)) {
            return DownloadCancellationResult(
                requestedSystemDownloadCount = 0,
                removedSystemDownloadCount = 0,
                statusUpdated = false
            )
        }

        val removedCount = runCatching {
            systemDownloadRemover.remove(longArrayOf(record.downloadId))
        }.getOrDefault(0).coerceAtLeast(0)
        val statusUpdated = if (removedCount > 0) {
            downloadRecordRepository.updateStatus(record.downloadId, DownloadStatus.CANCELED)
        } else {
            false
        }
        return DownloadCancellationResult(
            requestedSystemDownloadCount = 1,
            removedSystemDownloadCount = removedCount,
            statusUpdated = statusUpdated
        )
    }
}
