package com.example.videobrowser.download

data class DownloadStatusSnapshot(
    val status: DownloadStatus,
    val statusReason: Int? = null,
    val bytesDownloaded: Long? = null,
    val totalBytes: Long? = null
)

class DownloadStatusSynchronizer(
    private val repository: DownloadRecordRepository,
    private val querySnapshot: (Long) -> DownloadStatusSnapshot?
) {
    fun refresh(records: List<DownloadRecord> = repository.records()): List<DownloadRecord> {
        records
            .filter { record -> record.status == DownloadStatus.IN_PROGRESS }
            .forEach { record ->
                val snapshot = querySnapshot(record.downloadId) ?: return@forEach
                repository.updateSnapshot(
                    downloadId = record.downloadId,
                    status = snapshot.status,
                    statusReason = snapshot.statusReason,
                    bytesDownloaded = snapshot.bytesDownloaded,
                    totalBytes = snapshot.totalBytes
                )
            }
        return repository.records()
    }
}
